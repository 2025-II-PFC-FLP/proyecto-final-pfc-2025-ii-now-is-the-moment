package taller

import scala.collection.parallel.CollectionConverters._
import scala.util.Random

object RiegoParalelo {
  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

  // Funciones de acceso a los datos del tablón
  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3

  // Generación de entradas aleatorias (se reutiliza comportamiento)
  private val random = new Random()
  def fincaAlAzar(long: Int): Finca = {
    Vector.fill(long) {
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1,
        random.nextInt(4) + 1)
    }
  }

  def distanciaAlAzar(long: Int): Distancia = {
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i))
  }

  // tIR — tiempo inicio riego (la dependencia temporal es secuencial por definición)
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length

    // calculamos los tiempos de inicio recorriendo la programacion en orden
    def calcularTiempos(j: Int, tiempoAcumulado: Int, resultados: Vector[Int]): Vector[Int] = {
      if (j >= n) resultados
      else {
        val tablonActual = pi(j)
        val tiempoInicio = if (j == 0) 0 else tiempoAcumulado
        val tiempoRiego = treg(f, tablonActual)
        val nuevosResultados = resultados.updated(tablonActual, tiempoInicio)
        calcularTiempos(j + 1, tiempoInicio + tiempoRiego, nuevosResultados)
      }
    }

    calcularTiempos(0, 0, Vector.fill(n)(0))
  }

  // costoRiegoTablon (idéntico a secuencial)
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val tiempos = tIR(f, pi)
    val tInicio = tiempos(i)
    val ts = tsup(f, i)
    val tr = treg(f, i)
    val p  = prio(f, i)

    if (ts - tr >= tInicio) ts - (tInicio + tr)
    else p * ((tInicio + tr) - ts)
  }

  // costoRiegoFinca: calculamos el costo por tablón en paralelo y sumamos
  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    val n = f.length
    if (n == 0) 0
    else {
      // índices de tablones 0..n-1
      val indices = (0 until n).toVector
      // map en paralelo para cada tablón calcular su costo (tIR internamente calcula tiempos secuencialmente)
      indices.par.map(i => costoRiegoTablon(i, f, pi)).sum
    }
  }

  // costoMovilidad: suma de distancias entre entradas consecutivas de la programación.
  // La suma en sí es asociativa, así que podemos mapear y sumar (paralelizable).
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val n = pi.length
    if (n <= 1) 0
    else {
      val pairs = (0 until (n - 1)).toVector
      pairs.par.map { j =>
        val tablonActual = pi(j)
        val tablonSiguiente = pi(j + 1)
        d(tablonActual)(tablonSiguiente)
      }.sum
    }
  }

  // generarProgramacionesRiego: usamos el iterador de permutaciones de la colección estándar,
  // y devolvemos un Vector[ProgRiego]. No paralelizamos la generación porque es producción de la colección,
  // pero en ProgramacionRiegoOptimo procesaremos las permutaciones en paralelo.
  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    val n = f.length
    val indices = (0 until n).toVector
    if (n == 0) Vector.empty
    else indices.permutations.map(_.toVector).toVector
  }

  // ProgramacionRiegoOptimo: evaluamos todas las permutaciones en paralelo y reducimos buscando mínimo
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val n = f.length
    if (n == 0) (Vector.empty[Int], Int.MaxValue)
    else {
      // Generador de permutaciones como Iterator; lo convertimos a Vector para poder paralelizar el map.
      val indices = (0 until n).toVector
      val todas: Vector[ProgRiego] = indices.permutations.map(_.toVector).toVector

      // Para cada programación calculamos su costo total; procesamos en paralelo
      val costosPar: Seq[(ProgRiego, Int)] = todas.par.map { pi =>
        val cr = costoRiegoFinca(f, pi)
        val cm = costoMovilidad(f, pi, d)
        (pi, cr + cm)
      }.toList // traer a memoria como lista (resultado paralelo ya calculado)

      // Seleccionar el par de mínimo costo (siempre hay al menos uno)
      costosPar.minBy(_._2)
    }
  }

  // Función auxiliar (para debugging / presentación)
  def mostrarProgramacion(f: Finca, pi: ProgRiego, d: Distancia): Unit = {
    println(s"Programación: ${pi.mkString("[", ", ", "]")}")
    println(s"Tiempos inicio: ${tIR(f, pi).mkString("[", ", ", "]")}")
    println(s"Costo riego: ${costoRiegoFinca(f, pi)}")
    println(s"Costo movilidad: ${costoMovilidad(f, pi, d)}")
    println(s"Costo total: ${costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)}")
    println("---")
  }

  // Ejemplo de uso (idéntico a la versión secuencial)
  def ejemploDelPDF(): Unit = {
    println("=== EJEMPLO 1 DEL PDF (PARALELO) ===")

    val F1: Finca = Vector(
      (10, 3, 4),
      (5, 3, 3),
      (2, 2, 1),
      (8, 1, 1),
      (6, 4, 2)
    )

    val DF1: Distancia = Vector(
      Vector(0, 2, 2, 4, 4),
      Vector(2, 0, 4, 2, 6),
      Vector(2, 4, 0, 2, 2),
      Vector(4, 2, 2, 0, 4),
      Vector(4, 6, 2, 4, 0)
    )

    val (optima, costoOptimo) = ProgramacionRiegoOptimo(F1, DF1)
    println(s"Programación óptima encontrada: ${optima.mkString("[", ", ", "]")}")
    println(s"Costo óptimo: $costoOptimo")
  }
}

