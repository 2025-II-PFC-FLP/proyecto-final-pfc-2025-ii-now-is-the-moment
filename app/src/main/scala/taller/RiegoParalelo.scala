package taller

import scala.collection.parallel.CollectionConverters._

object RiegoParalelo {

  // Importamos todo de RiegoOptimo para reusar los tipos (Finca, Tablon, etc.)
  // y las funciones secuenciales básicas si es necesario.
  import RiegoOptimo._

  // ========================================================
  // 3.1 Paralelizando el cálculo de costos
  // ========================================================

  /**
   * Calcula el costo total de riego en paralelo.
   * Utiliza .par para paralelizar la iteración sobre los tablones.
   */
  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
    val n = f.length
    if (n == 0) 0
    else {
      // Convertimos el rango a paralelo (.par)
      // Nota: costoRiegoTablon es secuencial, pero se ejecuta concurrentemente
      // para distintos 'i' en distintos hilos.
      (0 until n).par.map(i => costoRiegoTablon(i, f, pi)).sum
    }
  }

  /**
   * Calcula el costo de movilidad en paralelo.
   * Paraleliza la suma de las distancias de los tramos.
   */
  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val n = pi.length
    if (n <= 1) 0
    else {
      // Creamos pares de índices (j, j+1) y calculamos sus distancias en paralelo
      (0 until (n - 1)).par.map { j =>
        val tablonActual = pi(j)
        val tablonSiguiente = pi(j + 1)
        d(tablonActual)(tablonSiguiente)
      }.sum
    }
  }

  // ========================================================
  // 3.2 Paralelizando la generación de programaciones
  // ========================================================

  /**
   * Genera las programaciones posibles.
   * La generación de permutaciones es difícil de paralelizar eficientemente en su creación,
   * pero podemos devolver el vector para ser procesado luego.
   */
  def generarProgramacionesRiegoPar(f: Finca): Vector[ProgRiego] = {
    val n = f.length
    val indices = (0 until n).toVector
    if (n == 0) Vector.empty
    else indices.permutations.map(_.toVector).toVector
  }

  // ========================================================
  // 3.3 Paralelizando la programación óptima
  // ========================================================

  /**
   * Calcula la programación óptima procesando las opciones en paralelo.
   */
  def ProgramacionRiegoOptimoPar(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val n = f.length
    if (n == 0) (Vector.empty[Int], 0)
    else {
      // 1. Generamos todas las permutaciones (Secuencialmente, porque permutations es un iterador)
      val indices = (0 until n).toVector
      val todas: Vector[ProgRiego] = indices.permutations.map(_.toVector).toVector

      // 2. Procesamos el cálculo de costos en PARALELO (.par)
      val costosPar = todas.par.map { pi =>
        // Aquí llamamos a las versiones secuenciales o paralelas.
        // Como estamos dentro de un map paralelo, las tareas ya están distribuidas.
        // Usar las funciones secuenciales aquí suele ser más rápido para evitar overhead de grano muy fino,
        // pero usaremos las Par para cumplir el requisito de usar todo paralelo si se desea.
        // Sin embargo, lo más eficiente es paralelizar el "bucle externo" (este map).

        val cr = costoRiegoFinca(f, pi) // Usamos la secuencial interna, el paralelismo está en 'todas.par'
        val cm = costoMovilidad(f, pi, d)
        (pi, cr + cm)
      }

      // 3. Encontramos el mínimo (La reducción también se beneficia de la colección paralela)
      costosPar.minBy(_._2)
    }
  }
}