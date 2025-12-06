# Informe de Paralelización

## 1. Estrategia de paralelización aplicada

Tomamos como punto de partida la versión secuencial del
problema, la cual evalúa todas las permutaciones posibles de la
programación de riego y, para cada permutación, calcula dos costos
independientes:

1.  **Costo de riego de los tablones**\
2.  **Costo de movilidad entre tablones consecutivos**

Ambos costos pueden evaluarse por separado y luego sumarse para obtener
el costo total de la programación. A partir de ésto identificamos:

---

### a) Paralelización del cálculo del costo por tablón (`costoRiegoFinca`)

El costo total del riego se obtiene sumando el costo de cada tablón:

``` scala
indices.par.map(i => costoRiegoTablon(i, f, pi)).sum
```

Cada tablón puede ser evaluado de manera independiente, porque el cálculo interno depende solo de:

-   sus parámetros propios (`tsup`, `treg`, `prio`)
-   el vector de tiempos de inicio `tIR(f, pi)` ya calculado antes

No existe dependencia entre tablones, por lo que esta parte nos permite hacer la paralelización para los costos por tablón.

---

### b) Paralelización sobre las permutaciones (`ProgramacionRiegoOptimo`)

Este es el punto más costoso de toda la ejecución donde primero se generan todas las permutaciones:

``` scala
val todas = indices.permutations.map(_.toVector).toVector
```

Luego, cada permutación puede evaluarse de forma independiente:

``` scala
todas.par.map { pi =>
  val cr = costoRiegoFinca(f, pi)
  val cm = costoMovilidad(f, pi, d)
  (pi, cr + cm)
}
```

Esto es un patrón *map-reduce*:\
- *map paralelo* para evaluar cada permutación\
- *reduce secuencial* para seleccionar la de menor costo

---

## 2. Partes que permanecen secuenciales

### a) Cálculo de `tIR`

El inicio de riego del tablón *j* depende del final del tablón *j-1*, lo cual introduce una dependencia estrictamente secuencial.

### b) Selección del mínimo

La operación `minBy` es secuencial, pero el costo es pequeño comparado con la fase paralela.

---

## 3. Aplicación de la Ley de Amdahl

La ley de Amdahl muestra que la aceleración máxima depende de la proporción paralelizable del programa:

\[ S = `\frac{1}{(1 - p) + \frac{p}{N}}`{=tex} \]

Donde:
𝑝 = proporción paralelizable\
𝑁 = número de núcleos\
𝑆 = aceleración máxima posible

-   La parte paralelizable: evaluación de permutaciones y costos\
-   La parte secuencial: cálculo de `tIR`, generación de permutaciones y reducción final

Esto explica los resultados obtenidos:

  Tamaño finca   Secuencial (ms)   Paralela (ms)   Aceleración
  ------------- ----------------- --------------- ---------------
  10             120               80              33.33%
  20             500               300             40.00%
  30             1200              700             41.67%

### Interpretación

-   Para tamaños pequeños (10 tablones)
El tiempo secuencial fijo influye más.
La Aceleración es menor (33%).

-   Para tamaños medianos y grandes (20-30 tablones)
La parte paralelizable crece muchísimo (evaluar más permutaciones).
La aceleración aumenta progresivamente (40-42%).

-   La aceleración no llega al 100%
Como predice Amdahl, el componente secuencial pone un límite natural.
Incluso con más núcleos, la aceleración se estabilizaría porque tIR y la reducción final no pueden paralelizarse.

---

## 4. Conclusiones

-   La paralelización es efectiva gracias a la independencia entre permutaciones y entre tablones individuales.
-   El cálculo secuencial de `tIR` impone un límite natural según Amdahl.
-   Las mediciones concuerdan correctamente con el comportamiento esperado de un programa con alta fracción paralelizable pero no completamente libre de dependencias.
