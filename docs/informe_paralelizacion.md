# **Informe de paralelización**

El objetivo de este informe es describir la estrategia utilizada para paralelizar la solución al problema de programación óptima de riego, analizar las ganancias o pérdidas de rendimiento obtenidas y relacionar los resultados con la Ley de Amdahl.

---

# **1. Estrategia general utilizada para paralelizar**

La solución original del problema consiste en evaluar todas las posibles programaciones de riego de la finca (todas las permutaciones de los tablones) y calcular para cada una su costo total.
Como el costo se obtiene sumando:

1. **Costo de riego de cada tablón**
2. **Costo de movilidad entre tablones consecutivos**

y cada programación de riego es independiente de las demás, esta estructura encaja muy bien con el modelo de paralelización basado en *map-reduce*.

A partir de esto, se identificaron dos niveles de paralelización:

---

## **- Paralelización del cálculo del costo por tablón**

Dentro de `costoRiegoFinca`, la versión secuencial recorre cada tablón y calcula su costo individual. El cálculo para cada tablón es autónomo:
No depende de otros tablones, únicamente utiliza su tiempo de inicio (producido por `tIR`) y los valores propios del tablón.

Esto permite reemplazar el recorrido secuencial:

```scala
indices.map(...)
```

por una versión paralela:

```scala
indices.par.map(...)
```

de forma que cada núcleo del procesador evalúa el costo de diferentes tablones simultáneamente y este tipo de paralelización permite que los resultados se sumen de forma asociativa.

---

## **- Paralelización del análisis de cada permutación**

Una finca de tamaño **n** tiene **n! permutaciones** posibles.
Cada permutación representa un orden diferente de riego y, por tanto, un costo distinto, por lo que puede evaluarse completamente de manera independiente de las demás.

Esto se prestó perfectamente para paralelizar la operación más costosa de todo el programa:

```scala
todas.par.map { pi =>
    val cr = costoRiegoFinca(...)
    val cm = costoMovilidad(...)
    (pi, cr + cm)
}
```

Con esto, se calcula de manera simultánea una programación completa de riego, reduciendo potencialmente el tiempo total.

Finalmente, se selecciona la programación de menor costo mediante `minBy`, que es una reducción secuencial ligera comparada con el trabajo previo.

---

# **2. Partes del programa que no se pudieron paralelizar**

A pesar de haber paralelizado las dos secciones más intensivas del algoritmo, existen componentes que por su naturaleza permanecen secuenciales.

Estas partes son fundamentales para entender por qué la aceleración obtenida fue negativa.

---

## **- Cálculo del vector de tiempos de inicio `tIR`**

La función `tIR` toma una programación de riego y calcula, para cada tablón, el tiempo exacto en el que debe iniciarse su riego.
El cálculo está definido así:

* El primer tablón comienza en tiempo 0.
* El segundo debe esperar a que el primero termine.
* El tercero debe esperar a que termine el segundo.
* Y así sucesivamente.

Esto forma una **cadena estrictamente dependiente**, imposible de romper sin ser alterada además, Por diseño, `tIR` **es inherentemente secuencial**.

Como esta función se llama una vez por cada tablón de cada permutación, representa una fracción muy importante del tiempo total.

---

## **- Selección final del mínimo**

Aunque `minBy` es secuencial, su peso es mínimo comparado con el análisis costo–cálculo de las permutaciones.
Sin embargo, contribuye a la fracción secuencial total.

---

# **3. Resultados obtenidos y análisis**

Los tiempos medidos para tamaños 6, 7 y 8 fueron:

| Tamaño | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
| ------ | --------------- | ------------- | --------------- |
| 6      | 91,964          | 659,790       | -617,45         |
| 7      | 230,999         | 389,527       | -68,83          |
| 8      | 701,555         | 2.485,008     | -254,21         |

La aceleración negativa indica que la versión paralela es más lenta que la secuencial.
Esto puede parecer extraño, pero es completamente coherente con la teoría del paralelismo.

---

# **4. Interpretación según la Ley de Amdahl**

La Ley de Amdahl establece que:

[
S = \frac{1}{(1 - p) + \frac{p}{N}}
]

donde:

* ( p ) = fracción paralelizable
* ( N ) = número de núcleos
* ( S ) = ganancia máxima posible

Esta ley demuestra que si el componente secuencial ( (1 - p) ) es significativo, la aceleración se ve severamente limitada incluso si usamos muchísimos núcleos.

En este caso:
La fracción secuencial del programa es muy alta.

Principalmente debido a:

* `tIR` → secuencial
* generación de permutaciones → secuencial

El usar la paralelización implíca que se hagan muchos procesos adicionales como lo son:

* crear hilos,
* repartir el trabajo,
* coordinar los hilos entre sí,
* unir los resultados,
* manejar memoria adicional,
* sincronizar operaciones,
* balancear carga entre núcleos.

Todo ese proceso consume tiempo, incluso antes de empezar a ejecutar el cálculo real.
Ese tiempo extra es el overhead.

Debido a esto:

La parte paralelizable no es suficientemente grande como para compensar los costos añadidos del paralelismo.
Por lo tanto, la aceleración es negativa.

---

## **- Sobrecosto de las colecciones paralelas de Scala**

Las parallel collections:

* dividen la carga en fragmentos,
* coordinan el trabajo entre hilos,
* sincronizan al final.

---

## **- Evaluar una permutación no es una tarea tan pesada**

Aunque 6, 7 u 8 tablones producen muchas permutaciones:

* 6! = 720
* 7! = 5040
* 8! = 40320

el cálculo interno de cada permutación es relativamente pequeño:
pocos tablones, pocos cálculos aritméticos, y una llamada a `tIR`.

Esto significa que el trabajo por núcleo es pequeño, y **no justifica el overhead del paralelismo**.

---

## **- El paralelismo produce más asignaciones de memoria**

Las permutaciones se copian a vectores antes de procesarse.
Además, las colecciones paralelas crean estructuras internas adicionales.

El recolector de basura trabaja más.

Resultado: mayor tiempo de ejecución.

---

# **Conclusión**

Tras haber paralelizado las secciones más intensivas del algoritmo y evaluado los resultados, se concluye que:

* Para tamaños pequeños (6–8 tablones), **el paralelismo no mejora el rendimiento**, sino que lo empeora notablemente.
* Esto ocurre porque la fracción secuencial del algoritmo es grande y la carga paralelizable por núcleo es relativamente pequeña.
* La sobrecarga de uso de colecciones paralelas supera ampliamente los beneficios.
* Los resultados obtenidos concuerdan perfectamente con la Ley de Amdahl, que predice que cuando la fracción secuencial es alta, el rendimiento paralelo estará limitado o incluso será peor.

En consecuencia, la paralelización comienza a ser realmente útil únicamente para fincas mucho más grandes, donde el número de permutaciones y el trabajo computacional por evaluación crecen lo suficiente como para justificar el paralelismo.

---