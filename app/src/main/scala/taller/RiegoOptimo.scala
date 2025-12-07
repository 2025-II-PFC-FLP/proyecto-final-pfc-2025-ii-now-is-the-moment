package taller

import scala.annotation.tailrec
import scala.util.Random

object RiegoOptimo {
  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

  // Funciones de acceso a los datos del tablón
  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3

  // 2.1 Generación de entradas aleatorias
  val random = new Random()

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

  // 2.3 tIR — tiempo inicio riego
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length

    @tailrec
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

  // 2.4 costoRiegoTablon
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val tiempos = tIR(f, pi)
    val tInicio = tiempos(i)
    val ts = tsup(f, i)
    val tr = treg(f, i)
    val p = prio(f, i)

    if (ts - tr >= tInicio) ts - (tInicio + tr)
    else p * ((tInicio + tr) - ts)
  }

  // 2.4 costoRiegoFinca
  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    @tailrec
    def aux(i: Int, acum: Int): Int = {
      if (i == f.length) acum
      else aux(i + 1, acum + costoRiegoTablon(i, f, pi))
    }
    aux(0, 0)
  }

  // 2.4 costoMovilidad
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val n = pi.length

    @tailrec
    def aux(j: Int, acum: Int): Int = {
      if (j == n - 1) acum
      else {
        val tablonActual = pi(j)
        val tablonSiguiente = pi(j + 1)
        aux(j + 1, acum + d(tablonActual)(tablonSiguiente))
      }
    }

    if (n <= 1) 0 else aux(0, 0)
  }

  // 2.5 generarProgramacionesRiego
  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    val indices = (0 until f.length).toVector

    def permutaciones(elems: Vector[Int]): Vector[Vector[Int]] = {
      if (elems.isEmpty) Vector(Vector.empty)
      else {
        elems.flatMap { elem =>
          val resto = elems.filter(_ != elem)
          permutaciones(resto).map(elem +: _)
        }
      }
    }

    permutaciones(indices)
  }

  // 2.6 ProgramacionRiegoOptimo
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val todas = generarProgramacionesRiego(f)

    if (todas.isEmpty) (Vector(), Int.MaxValue)
    else {
      def encontrarOptimo(programaciones: Vector[ProgRiego], mejorPi: ProgRiego, mejorCosto: Int): (ProgRiego, Int) = {
        programaciones match {
          case Vector() => (mejorPi, mejorCosto)
          case pi +: resto =>
            val costoRiego = costoRiegoFinca(f, pi)
            val costoMov = costoMovilidad(f, pi, d)
            val costoTotal = costoRiego + costoMov

            if (costoTotal < mejorCosto) encontrarOptimo(resto, pi, costoTotal)
            else encontrarOptimo(resto, mejorPi, mejorCosto)
        }
      }

      val primeraPi = todas.head
      val primerCosto = costoRiegoFinca(f, primeraPi) + costoMovilidad(f, primeraPi, d)
      encontrarOptimo(todas.tail, primeraPi, primerCosto)
    }
  }

  // Funciones auxiliares para pruebas y depuración
  def mostrarProgramacion(f: Finca, pi: ProgRiego, d: Distancia): Unit = {
    println(s"Programación: ${pi.mkString("[", ", ", "]")}")
    println(s"Tiempos inicio: ${tIR(f, pi).mkString("[", ", ", "]")}")
    println(s"Costo riego: ${costoRiegoFinca(f, pi)}")
    println(s"Costo movilidad: ${costoMovilidad(f, pi, d)}")
    println(s"Costo total: ${costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)}")
    println("---")
  }
}