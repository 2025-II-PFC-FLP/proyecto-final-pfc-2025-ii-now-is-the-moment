// RiegoOptimo.scala — versión secuencial según taller

package taller

import scala.util.Random



object RiegoOptimo {
  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]
  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3

  // 2.3 tIR — tiempo inicio riego
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length
    val tiempos = Vector.fill(n)(0)

    def aux(j: Int, acumulado: Int, t: Vector[Int]): Vector[Int] = {
      if (j == n) t
      else {
        val indexTablon = pi.indexOf(j)
        val nuevoInicio = if (j == 0) 0 else acumulado
        val tActualizado = t.updated(indexTablon, nuevoInicio)
        val nuevoAcumulado = nuevoInicio + treg(f, indexTablon)
        aux(j + 1, nuevoAcumulado, tActualizado)
      }
    }

    aux(0, 0, tiempos)
  }

  // 2.4 costoRiegoTablon
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val tiempos = tIR(f, pi)
    val tInicio = tiempos(i)
    val (ts, tr, p) = f(i)

    if (ts - tr >= tInicio) ts - (tInicio + tr)
    else p * ((tInicio + tr) - ts)
  }

  // 2.4 costoRiegoFinca
  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    def aux(i: Int, acum: Int): Int = {
      if (i == f.length) acum
      else aux(i + 1, acum + costoRiegoTablon(i, f, pi))
    }
    aux(0, 0)
  }

  // 2.4 costoMovilidad
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val n = f.length

    def aux(j: Int, acum: Int): Int = {
      if (j == n - 1) acum
      else {
        val a = pi(j)
        val b = pi(j + 1)
        aux(j + 1, acum + d(a)(b))
      }
    }

    aux(0, 0)
  }

  // 2.5 generarProgramacionesRiego
  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    def perm(lst: Vector[Int]): Vector[Vector[Int]] = {
      if (lst.isEmpty) Vector(Vector())
      else {
        lst.indices.flatMap { i =>
          val elem = lst(i)
          val resto = lst.take(i) ++ lst.drop(i + 1)
          perm(resto).map(elem +: _)
        }.toVector
      }
    }
    perm((0 until f.length).toVector)
  }

  // 2.6 ProgramacionRiegoOptimo
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val todas = generarProgramacionesRiego(f)

    def aux(lst: Vector[ProgRiego], mejorPi: ProgRiego, mejorCosto: Int): (ProgRiego, Int) = {
      if (lst.isEmpty) (mejorPi, mejorCosto)
      else {
        val pi = lst.head
        val costo = costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)
        if (costo < mejorCosto) aux(lst.tail, pi, costo)
        else aux(lst.tail, mejorPi, mejorCosto)
      }
    }

    aux(todas.tail, todas.head, costoRiegoFinca(f, todas.head) + costoMovilidad(f, todas.head, d))
  }
}
