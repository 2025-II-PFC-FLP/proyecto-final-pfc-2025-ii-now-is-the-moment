# Informe de corrección proyecto final RiegoOptimo
# 1. Corrección de las funciones
Este informe detalla la corrección formal de las funciones implementadas en el proyecto final "RiegoOptimo".  

## 1.1 Corrección de `fincaAlAzar`
La función
```Scala
fincaAlAzar : Int → Finca
```

genera una finca con un número dado de tablones `n`, cada uno con características aleatorias.
### Especificación formal
Debe cumplirse:


$$\forall n \in \mathbb{N}, \text{fincaAlAzar}(n) = f \text{ tal que } |f| = n$$

y
$$
\forall i \in \{0, \dots, n-1\}, f(i) \text{ es un Tablón válido generado aleatoriamente.}
$$

### Argumento de corrección
La función utiliza un generador de números aleatorios para asignar características a cada tablón, asegurando que el número de tablones en la finca coincide con el parámetro de entrada.

Para hacer esto, se construye un vector de tamaño `n`, donde cada elemento es un tablón generado aleatoriamente y cada posición del vector corresponde a un tablón en la finca, cuyos valores se obtienen mediante llamadas al generador aleatorio.
### Conclusión
`fincaAlAzar` es correcta porque genera una finca con el número especificado de tablones.

## 1.2 Corrección de `distanciaAlAzar`
La función
```Scala
distanciaAlAzar : Int → Distancias
```
genera una matriz de distancias aleatorias entre `n` tablones.
### Especificación formal
Debe cumplirse:

### 1. Dimensión:

$$\forall n \in \mathbb{N}, \text{distanciaAlAzar}(n) = d \text{ tal que } d \in \mathbb{N}^{n \times n}$$

### 2. Diagonal nula:

$$\forall i \in \{0, \dots, n-1\}, d(i, i) = 0$$

### 3. Simetría:

$$\forall i, j, d(i, j) = d(j, i)$$

### 4. Valores aleatorios válidos:

$$\forall i \neq j, 1 \leq d(i, j) \leq 3n$$
### Argumento de corrección
La función crea una matriz cuadrada de tamaño `n x n`, llenando cada posición con valores generados aleatoriamente que representan las distancias entre los tablones.

En cada una de las posiciones `(i, j)` de la matriz, se asigna un valor de distancia generado aleatoriamente, asegurando que la matriz cumple con las dimensiones requeridas.

Luego, mediante `vector.tabulate` se genera la matriz final `d` con estas características:
- La diagonal se establece en 0.
- La simetría se garantiza asignando `d(i, j) = d(j, i)`.
- Los valores fuera de la diagonal se generan aleatoriamente dentro del rango especificado
### Conclusión
`distanciaAlAzar` es correcta porque genera una matriz de distancias con las dimensiones especificadas.

## 1.3 Corrección de `tIR` y `calcularTiempos`
La función
```Scala
tIR : (Finca, Permutación) → TiempoInicioRiego
```
calcula los tiempos de inicio de riego para cada tablón en una finca según la programación dada en `pi`.

Para esto, emplea una función recursiva auxiliar `calcularTiempos` que acumula los tiempos de riego siguiendo el orden de la permutación `pi`.
```Scala
calcularTiempos : (j, acum, Vector[Int]) → Vector[Int]
```
### Especificación formal
Sean:
- `f` una finca de tamaño `n`
- `pi` una permutación de `{0,…,n−1}`
- `t=tIR(f,pi)` el vector resultante

Debe cumplirse:
### 1. Longitud
$$|t|=n$$
### 2. Tiempo de inicio según la permutación

Para cada posición `j` de la programación:

Si `pi(j)=k`, entonces:

$$t[k]=\sum_{m=0}^{j−1} treg(f,\pi(m))$$

Es decir:
- el primer tablón en pi empieza en 0
- cada tablón comienza cuando termina el riego del anterior en la programación

### 3. Unicidad
$$\forall k, t[k] \text{ está definido exactamente una vez}$$
### Argumento de corrección
La función recorre la permutación de tablones, acumulando los tiempos de riego de los tablones anteriores para calcular el tiempo de inicio de cada tablón.

`tIR` llama a `calcularTiempos`:
```Scala
calcularTiempos(0, 0, Vector.fill(n)(0))
```
donde:
- `j == 0`: comienza con el primer tablón de la permutación.
- `acum = 0`: el tiempo acumulado inicial.
- `Vector.fill(n)(0)`: vector de tiempos inicializado a cero.

El auxiliar `calcularTiempos` funciona así:
- ### Caso base: 
    Si `j == n`, retorna el vector de tiempos acumulados.
- ### Caso recursivo:
  - Obtiene el índice del tablón actual `k = pi(j)`.
  - Asigna el tiempo acumulado actual al vector en la posición `k` `t[k] = acum`
  - Actualiza el tiempo acumulado sumando el tiempo de riego del tablón actual.
  - Llama recursivamente a `calcularTiempos` con `j + 1`.

### Conclusión
`tIR` es correcta porque:
- Calcula los tiempos de inicio según la permutación `pi`.
- El auxiliar calcularTiempos implementa correctamente la suma acumulada de tiempos.
- El resultado cumple con la especificación formal en longitud, unicidad y definición del tiempo de inicio.
- Cada tablón tiene un tiempo de inicio único y correctamente calculado.
## 1.4 Corrección de `costoRiegoTablon`
La función
```Scala
costoRiegoTablon : (Int, Finca, Permutación) → Int
```
calcula el costo de riego de un tablón específico en una finca dada una permutación `pi` (solo para obtener los tiempos de inicio).
### Especificación formal
Sea:
- `f` una finca de tamaño `n`
- `pi` una permutación de `{0,…,n−1}`
- `i` un tablón en `{0,…,n−1}`
- `t = tIR(f, pi)` el vector de tiempos de inicio
- `tInicio = t[i]` el tiempo de inicio del tablón `i`
- `ts = tsup(f, i)` el tiempo de soporte del tablón `i`
- `tr = treg(f, i)` el tiempo de riego del tablón `i`
- `p = prio(f, i)` la prioridad del tablón `i`

Debe cumplirse:
$$
\text{costoRiegoTablon}(i, f, \pi) =
\begin{cases}
t_s - (t_{\text{Inicio}} + t_r) & \text{si } t_s - t_r \ge t_{\text{Inicio}} \\
p \cdot ((t_{\text{Inicio}} + t_r) - t_s) & \text{si } t_s - t_r < t_{\text{Inicio}}
\end{cases}
$$
### Argumento de corrección
1. Se obtienen los tiempos de inicio `t` usando `tIR(f, pi)`.
2. Se calcula el costo de riego del tablón `i` según la fórmula especificada, considerando los tiempos de soporte, riego y prioridad.
3. Se aplican las condiciones para determinar si el costo es positivo o negativo según los tiempos calculados.
4. Se retorna el costo calculado.
### Conclusión
`costoRiegoTablon` es correcta porque:
- Utiliza el tiempo de inicio generado por la programación `pi`
- Aplica la fórmula definida para el costo de riego del tablón `i`
- Retorna el valor calculado según las reglas del modelo.

## 1.5 Corrección de `costoRiegoFinca`
La función
```Scala
costoRiegoFinca : (Finca, Permutación) → Int
``` 
calcula el costo total de riego de una finca `f`, usando una permutación `pi` de tablones para determinar los tiempos de inicio de los tablones a través de `costoRiegoTablon`.
### Especificación formal
Sea `f` una finca de tamaño `n` 
Debe cumplirse:
$$
\text{costoRiegoFinca}(f, \pi) = \sum_{i=0}^{n-1} \text{costoRiegoTablon}(i, f, \pi)
$$
### Argumento de corrección
La función define un auxiliar recursivo
```Scala
aux : (i, acum) → Int
```
El auxiliar funciona así:
- ### Caso base: 
    Si `i == f.length`, retorna el acumulador, que contiene la suma de todos los costos hasta ese punto.

- ### Caso recursivo:
  Suma el costo del tablón `i` `costoRiegoTablon(i, f, pi)` al acumulador, y llama recursivamente a `aux(i + 1, acum + costo)`.
  
Finalmente, costoRiegoFinca invoca al auxiliar con:
```Scala
aux(0, 0)
```
lo que corresponde exactamente a la sumatoria definida en la especificación formal.

### Conclusión
`costoRiegoFinca` es correcta porque:
- Recorre todos los tablones de la finca
- Suma exactamente sus costos individuales
- Y el resultado coincide con la definición formal del costo total de riego.

## 1.6 Corrección de `costoMovilidad`
La función
```Scala
costoMovilidad : (Finca, Permutación, Distancia) → Int
```
calcula el costo de movilidad entre tablones en una finca dada una permutación `pi` y una matriz de distancias `d`.
### Especificación formal
Sea `pi` una permutación de tamaño `n`.Debe cumplirse:
$$
\text{costoMovilidad}(f, \pi, d) = \sum_{j=0}^{n-2} d(\pi_{j}, \pi_{j+1})
$$
### Argumento de corrección
La función implementa esta definición recorriendo la permutación desde el índice 0 hasta el índice n−2. Para cada par consecutivo
$\pi[j],\pi[j+1]$:
- Obtiene su distancia desde la matriz `d`.
- La acumula.
- Avanza recursivamente al siguiente índice.

El caso base se alcanza en `j == n−1`, momento en el que se detiene la recursión.
### Conclusión
`costoMovilidad` es correcta porque
- Evalúa exactamente los pares consecutivos $(\pi[j],\pi[j+1])$ según su definición formal.
- La recursión cubre el rango completo de índices del enunciado.
- El resultado coincide con la especificación matemática del costo de movilidad.

## 1.7 Corrección de `generarProgramacionesRiego`
La función
```Scala
generarProgramacionesRiego : Finca → Vector[ProgRiego]
```
genera todas las permutaciones posibles de los índices de los tablones de la finca `f`. Cada permutación representa una posible programación de riego válida.
### Especificación formal
Sea `f` una finca de tamaño `n`.
Definimos el conjunto de programaciones posibles como el conjunto de todas las permutaciones del conjunto:
$$ I =\{0, 1, 2, \dots, n-1\}
$$
La función debe producir
$$\text{generarProgramacionesRiego}(f) = Perm(I)
$$donde `Perm(I)` es el conjunto de todas las permutaciones de `I`.

Entonces, debe generar exactamente las $n!$ permutaciones sin omitir ninguna ni repetir.
### Argumento de corrección
La función utiliza un algoritmo recursivo que
1. Si el conjunto de índices está vacío, retorna una lista con la permutación vacía.
2. Para cada índice `i` en el conjunto:
   - Lo selecciona como el primer elemento de la permutación.
   - Genera recursivamente todas las permutaciones de los índices restantes.
   - Combina `i` con cada permutación generada de los índices restantes para formar nuevas permutaciones completas.
3. Acumula todas las permutaciones generadas y las retorna.

### Conclusión
`generarProgramacionesRiego` es correcta porque:
- Genera todas las permutaciones posibles de los índices de los tablones.
- No omite ninguna permutación ni repite ninguna.
- El número total de permutaciones generadas es exactamente $n!$, cumpliendo con la especificación formal.
- Cada permutación generada es una programación de riego válida para la finca `f`.

## 1.8 Corrección de `ProgramacionRiegoOptimo`
La función
```Scala
ProgramacionRiegoOptimo : (Finca, Distancia) → (Permutación, Int)
``` 
encuentra la programación de riego óptima, es decir, la permutación que minimiza el costo total.
### Especificación formal
- Sea `f` una finca con `n` tablones.
- Sea `d` una matriz de distancias entre los tablones.
  - Sea $\Pi$ una permutación de los índices `{0,…,n−1}`.

Debe cumplirse:
$$
\text{ProgramacionRiegoOptimo}(f, d) = (\pi^*, C^*)\text{ tal que }  C^* = \min_{\pi\in\Pi} (\text{costoRiegoFinca}(f, \pi) + \text{costoMovilidad}(f, \pi, d))
$$
### Argumento de corrección
La función genera todas las permutaciones posibles, calcula el costo total para cada una y selecciona la que tiene el costo mínimo.
1. Genera todas las permutaciones `pi` usando `generarProgramacionesRiego(f)`.
2. Para cada permutación `pi`, calcula el costo total:
   $$C(\pi) = \text{costoRiegoFinca}(f, \pi) + \text{costoMovilidad}(f, \pi, d)$$
3. Se mantiene un par `(mejorPi, mejorCosto)` que representa hasta ese momento la permutación de menor costo.
4. El algoritmo revisa todas las permutaciones sin omitir ninguna, actualizando el mínimo cuando encuentra un costo mejor.
5. Finalmente, retorna la permutación con el costo mínimo y el costo asociado.
### Conclusión
`ProgramacionRiegoOptimo` es correcta porque:
- Evalúa todas las permutaciones posibles.
- Calcula correctamente el costo total para cada permutación.
- Selecciona la permutación con el costo mínimo, cumpliendo con la especificación formal.
- Retorna la permutación óptima junto con su costo asociado.

# 2. Conclusión General
Se demostró la corrección de todas las funciones mediante:
- Correspondencia con definiciones matemáticas formales.
- Implementación correcta de algoritmos conocidos.
- Aseguramiento de que cada función cumple con su especificación formal.
