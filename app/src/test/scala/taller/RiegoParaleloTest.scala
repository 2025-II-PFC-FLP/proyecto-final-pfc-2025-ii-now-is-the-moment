package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoParaleloTest extends AnyFunSuite {

  // Importamos tanto la versión secuencial (para comparar) como la paralela
  import RiegoOptimo._
  import RiegoParalelo._

  // ==========================================
  // DATOS DE PRUEBA
  // ==========================================

  // Finca del Ejemplo 1 del PDF
  val fincaPDF: Finca = Vector(
    (10, 3, 4), // T0
    (5, 3, 3),  // T1
    (2, 2, 1),  // T2
    (8, 1, 1),  // T3
    (6, 4, 2)   // T4
  )

  val distPDF: Distancia = Vector(
    Vector(0, 2, 2, 4, 4),
    Vector(2, 0, 4, 2, 6),
    Vector(2, 4, 0, 2, 2),
    Vector(4, 2, 2, 0, 4),
    Vector(4, 6, 2, 4, 0)
  )

  // ==========================================
  // TESTS PARALELOS
  // ==========================================

  test("costoRiegoFincaPar debe dar el mismo resultado que la secuencial") {
    // Probamos con una permutación arbitraria
    val pi = Vector(0, 1, 2, 3, 4)
    val costoSeq = costoRiegoFinca(fincaPDF, pi)
    val costoPar = costoRiegoFincaPar(fincaPDF, pi)

    assert(costoPar == costoSeq, s"El costo paralelo ($costoPar) difiere del secuencial ($costoSeq)")
  }

  test("costoMovilidadPar debe dar el mismo resultado que la secuencial") {
    val pi = Vector(4, 3, 2, 1, 0)
    val costoSeq = costoMovilidad(fincaPDF, pi, distPDF)
    val costoPar = costoMovilidadPar(fincaPDF, pi, distPDF)

    assert(costoPar == costoSeq, s"El costo movilidad paralelo ($costoPar) difiere del secuencial ($costoSeq)")
  }

  test("ProgramacionRiegoOptimoPar debe encontrar el mismo COSTO MÍNIMO que la secuencial") {
    // No necesariamente la misma permutación (pueden haber varias óptimas), pero sí el mismo costo.
    val (_, costoMinSeq) = ProgramacionRiegoOptimo(fincaPDF, distPDF)
    val (progPar, costoMinPar) = ProgramacionRiegoOptimoPar(fincaPDF, distPDF)

    assert(costoMinPar == costoMinSeq, s"El óptimo paralelo ($costoMinPar) no coincide con el secuencial ($costoMinSeq)")

    // Verificamos que el costo reportado sea real calculándolo de nuevo
    val costoRealCalculado = costoRiegoFinca(fincaPDF, progPar) + costoMovilidad(fincaPDF, progPar, distPDF)
    assert(costoMinPar == costoRealCalculado, "El costo devuelto por la función óptima no coincide con el cálculo real de esa programación")
  }

  test("Prueba de estrés ligera: Finca aleatoria") {
    // Generamos una finca pequeña aleatoria para verificar que no explota el paralelismo
    val len = 6
    val f = fincaAlAzar(len)
    val d = distanciaAlAzar(len)

    val (prog, costo) = ProgramacionRiegoOptimoPar(f, d)

    assert(prog.length == len)
    assert(prog.toSet == (0 until len).toSet) // Debe ser una permutación válida
  }
}