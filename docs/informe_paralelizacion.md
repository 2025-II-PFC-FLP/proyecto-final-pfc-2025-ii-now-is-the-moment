# INFORME DE PARALELIZACIÓN

El objetivo de este informe es describir la estrategia utilizada para paralelizar el cálculo de la programación óptima de riego, analizar los resultados obtenidos tras aplicar paralelización con Scala Parallel Collections y explicar dichos resultados utilizando la Ley de Amdahl. Además, se presentan y analizan los tiempos reales medidos para las versiones secuencial y paralela del algoritmo.

---

# 1. Estrategia general de paralelización

El problema consiste en evaluar todas las posibles programaciones de riego (todas las permutaciones de la finca) y calcular el costo total de cada una. El costo depende de:

1. El **costo de riego de cada tablón**.
2. El **costo de movilidad entre tablones consecutivos**.

Cada permutación de tablones es independiente, por lo que este problema encaja naturalmente en un esquema **map–reduce**.

La paralelización se implementó en dos niveles.

---

## 1.1 Paralelización del costo por tablón

La versión secuencial recorre los tablones haciendo:

```scala
indices.map(i => costoRiegoTablon(i, f, pi))
```

La versión paralela reemplaza este recorrido por:

```scala
indices.par.map(i => costoRiegoTablon(i, f, pi))
```

Cada núcleo calcula el costo de distintos tablones simultáneamente.  
Dado que la suma final de costos es asociativa, esta sección se paraleliza correctamente.

---

## 1.2 Paralelización del análisis de todas las permutaciones

Cada permutación de tablones representa una programación completa y puede evaluarse de forma independiente.  

Esto permite paralelizar la operación más costosa:

```scala
todas.par.map { pi =>
  val cr = costoRiegoFinca(f, pi)
  val cm = costoMovilidad(f, pi, d)
  (pi, cr + cm)
}
```

La reducción final mediante `minBy` selecciona la permutación de menor costo.

---

# 2. Componentes que no pueden paralelizarse

Aunque gran parte del algoritmo sí puede paralelizarse, existen componentes que **son inherentemente secuenciales** y limitan la aceleración global.

---

## 2.1 El cálculo de tiempos de inicio `tIR`

El vector de tiempos de inicio se calcula secuencialmente:

- El tablón 0 inicia en tiempo 0.
- El tablón 1 inicia cuando termina el tablón 0.
- El tablón 2 inicia cuando termina el 1.
- Y así sucesivamente.

Por definición, es una cadena estrictamente dependiente.

Este cálculo se ejecuta **una vez por cada permutación**, lo que aumenta la fracción secuencial.

---

## 2.2 La generación de permutaciones

Scala genera las permutaciones de forma secuencial.  
Aunque las evaluemos después en paralelo, la creación de estas estructuras internas no puede paralelizarse.

---

## 2.3 La reducción `minBy`

La selección final de la permutación óptima también es secuencial, aunque su impacto es bajo comparado con el resto del trabajo.

---

# 3. Resultados obtenidos y análisis 

Las pruebas se realizaron con tamaños de 6 a 10 tablones.  
Los tiempos obtenidos fueron:

| Tamaño | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
|-------|------------------|----------------|------------------|
| 6     | 100.34           | 516.02         | -414.25          |
| 7     | 104.20           | 478.29         | -359.00          |
| 8     | 1017.07          | 1977.36        | -94.42           |
| 9     | 8341.79          | 9360.26        | -12.21           |
| 10    | 149484.57        | 71643.28       | 52.07            |

La aceleración se calculó mediante:

$$
\text{Aceleración} =
\left( \frac{T_\text{sec} - T_\text{par}}{T_\text{sec}} \right) \times 100
$$

---

## 3.1 Interpretación directa

- De tamaños **6–9**, la aceleración es **negativa** → la versión paralela es más lenta.
- En tamaño **10**, aparece la primera mejora real (+52%).

Esto es coherente: el número de permutaciones crece muy rápido:

- 6! = 720  
- 7! = 5040  
- 8! = 40,320  
- 9! = 362,880  
- 10! = 3,628,800  

A partir de 10 tablones, el trabajo por núcleo es lo suficientemente grande como para compensar el overhead del paralelismo.

---

# 4. Interpretación según la Ley de Amdahl

La Ley de Amdahl se expresa como:

$$
S = \frac{1}{(1 - p) + \frac{p}{N}}
$$

donde:

- \( p \) = fracción paralelizable  
- \( N \) = número de núcleos  
- \( S \) = aceleración máxima posible  

Si la fracción secuencial \( (1 - p) \) es alta, la aceleración total será baja, incluso si \( N \) es grande.

En nuestro programa:

- `tIR` es secuencial  
- la generación de permutaciones es secuencial  
- hay overhead significativo por el uso de colecciones paralelas  

Por tanto:

- Para tamaños pequeños, el paralelismo **produce más trabajo extra que beneficios**, causando aceleración negativa.
- Solo cuando el número de permutaciones es enorme (como en n = 10), el trabajo paralelizable domina y se obtiene una mejora real.

Esto coincide completamente con Amdahl:  
**una porción secuencial suficientemente grande limita la aceleración total.**

---

# 5. Observaciones sobre el overhead

El overhead adicional de las colecciones paralelas incluye:

- creación de hilos,
- sincronización,
- balanceo de carga,
- estructuras auxiliares internas,
- incremento en la presión del recolector de basura,
- copia de permutaciones a nuevos vectores.

Para permutaciones pequeñas, este overhead domina.  
Para permutaciones grandes, el costo total crece de manera factorial y el overhead se vuelve pequeño comparado con el trabajo real.

---

# 6. Conclusión

Tras paralelizar las partes más importantes del algoritmo y medir los resultados:

- La versión paralela es más lenta para fincas pequeñas (6–9 tablones).
- Únicamente a partir de tamaños grandes (como 10 tablones) surge un beneficio real.
- Esto ocurre porque la fracción secuencial del algoritmo es alta y la carga por núcleo es baja cuando el número de permutaciones es reducido.
- Los resultados coinciden exactamente con lo predicho por la Ley de Amdahl.
- La paralelización solo es realmente útil cuando el costo total del trabajo paralelizable crece lo suficiente como para compensar el overhead asociado.

La implementación paralela es correcta, pero su eficiencia depende fuertemente del tamaño de la entrada.
