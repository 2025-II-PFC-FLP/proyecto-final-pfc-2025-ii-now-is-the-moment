package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoParaleloTest extends AnyFunSuite {
//prueva
  import RiegoOptimo._
  import RiegoParalelo._

  val fincaPDF: Finca = Vector(
    (10, 3, 4),
    (5, 3, 3),
    (2, 2, 1),
    (8, 1, 1),
    (6, 4, 2)
  )

  val distPDF: Distancia = Vector(
    Vector(0, 2, 2, 4, 4),
    Vector(2, 0, 4, 2, 6),
    Vector(2, 4, 0, 2, 2),
    Vector(4, 2, 2, 0, 4),
    Vector(4, 6, 2, 4, 0)
  )

  test("costoRiegoFincaPar: caso simple secuencial vs paralelo") {
    val pi = Vector(0, 1, 2, 3, 4)
    assert(costoRiegoFincaPar(fincaPDF, pi) == costoRiegoFinca(fincaPDF, pi))
  }

  test("costoRiegoFincaPar: permutación invertida") {
    val pi = Vector(4, 3, 2, 1, 0)
    assert(costoRiegoFincaPar(fincaPDF, pi) == costoRiegoFinca(fincaPDF, pi))
  }

  test("costoRiegoFincaPar: permutación aleatoria") {
    val pi = Vector(1, 3, 0, 4, 2)
    assert(costoRiegoFincaPar(fincaPDF, pi) == costoRiegoFinca(fincaPDF, pi))
  }

  test("costoRiegoFincaPar: finca aleatoria pequeña") {
    val f = fincaAlAzar(4)
    val pi = Vector(0, 1, 2, 3)
    assert(costoRiegoFincaPar(f, pi) == costoRiegoFinca(f, pi))
  }

  test("costoRiegoFincaPar: finca de 1 tablón") {
    val f = Vector((5, 2, 3))
    val pi = Vector(0)
    assert(costoRiegoFincaPar(f, pi) == costoRiegoFinca(f, pi))
  }

  test("costoMovilidadPar: básico coincide con secuencial") {
    val pi = Vector(0, 1, 2, 3, 4)
    assert(costoMovilidadPar(fincaPDF, pi, distPDF) == costoMovilidad(fincaPDF, pi, distPDF))
  }

  test("costoMovilidadPar: orden invertido coincide") {
    val pi = Vector(4, 3, 2, 1, 0)
    assert(costoMovilidadPar(fincaPDF, pi, distPDF) == costoMovilidad(fincaPDF, pi, distPDF))
  }

  test("costoMovilidadPar: permutación intermedia coincide") {
    val pi = Vector(2, 4, 1, 0, 3)
    assert(costoMovilidadPar(fincaPDF, pi, distPDF) == costoMovilidad(fincaPDF, pi, distPDF))
  }

  test("costoMovilidadPar: finca aleatoria") {
    val f = fincaAlAzar(5)
    val d = distanciaAlAzar(5)
    val pi = Vector(0, 1, 2, 3, 4)
    assert(costoMovilidadPar(f, pi, d) == costoMovilidad(f, pi, d))
  }

  test("costoMovilidadPar: finca de un tablón (0 costo)") {
    val f = Vector((3, 1, 1))
    val d = Vector(Vector(0))
    val pi = Vector(0)
    assert(costoMovilidadPar(f, pi, d) == 0)
  }

  test("generarProgramacionesRiegoPar: tamaño 0") {
    assert(generarProgramacionesRiegoPar(Vector()).isEmpty)
  }

  test("generarProgramacionesRiegoPar: tamaño 1") {
    val f = Vector((5, 2, 3))
    assert(generarProgramacionesRiegoPar(f) == Vector(Vector(0)))
  }

  test("generarProgramacionesRiegoPar: tamaño 2 produce 2 permutaciones") {
    val f = Vector((1, 1, 1), (2, 2, 2))
    val esperado = Vector(Vector(0,1), Vector(1,0))
    val obtenido = generarProgramacionesRiegoPar(f).sortBy(_.mkString)
    assert(obtenido == esperado)
  }

//

  test("generarProgramacionesRiegoPar produce todas las permutaciones sin repetir") {
    val f = fincaAlAzar(4)
    val perms = generarProgramacionesRiegoPar(f)
    assert(perms.distinct.size == perms.size)
    assert(perms.size == 24)
  }


  test("ProgramacionRiegoOptimoPar da mismo costo mínimo que el secuencial (PDF)") {
    val (_, cSeq) = ProgramacionRiegoOptimo(fincaPDF, distPDF)
    val (_, cPar) = ProgramacionRiegoOptimoPar(fincaPDF, distPDF)
    assert(cSeq == cPar)
  }

  test("ProgramacionRiegoOptimoPar: finca aleatoria tamaño 5") {
    val f = fincaAlAzar(5)
    val d = distanciaAlAzar(5)
    val (_, cSeq) = ProgramacionRiegoOptimo(f, d)
    val (_, cPar) = ProgramacionRiegoOptimoPar(f, d)
    assert(cSeq == cPar)
  }

  test("ProgramacionRiegoOptimoPar: finca de tamaño 1") {
    val f = Vector((4,2,3))
    val d = Vector(Vector(0))
    val (pi, costo) = ProgramacionRiegoOptimoPar(f, d)
    assert(pi == Vector(0))
    assert(costo == 4 - (0 + 2))
  }

  test("ProgramacionRiegoOptimoPar encuentra un costo válido real") {
    val f = fincaAlAzar(4)
    val d = distanciaAlAzar(4)
    val (pi, costo) = ProgramacionRiegoOptimoPar(f, d)

    val costoReal = costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)
    assert(costo == costoReal)
  }

  test("ProgramacionRiegoOptimoPar retorna permutación válida") {
    val f = fincaAlAzar(5)
    val d = distanciaAlAzar(5)

    val (pi, _) = ProgramacionRiegoOptimoPar(f, d)
    assert(pi.toSet == (0 until 5).toSet)
  }
}
