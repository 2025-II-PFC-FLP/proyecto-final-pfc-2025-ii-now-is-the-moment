package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoOptimoTest extends AnyFunSuite {

  // Importamos las funciones del objeto principal.
  // Asegúrate de que tu objeto se llame RiegoOptimo
  import RiegoOptimo._

  // ==========================================
  // DATOS DE PRUEBA (FIXTURES)
  // ==========================================

  // Finca Pequeña (3 tablones)
  // T0: (10, 3, 4) -> Sobrevive 10, Riego 3, Prio 4
  // T1: (5, 3, 3)  -> Sobrevive 5, Riego 3, Prio 3
  // T2: (2, 2, 1)  -> Sobrevive 2, Riego 2, Prio 1
  val fincaPeque: Finca = Vector(
    (10, 3, 4),
    (5, 3, 3),
    (2, 2, 1)
  )

  val distPeque: Distancia = Vector(
    Vector(0, 2, 4),
    Vector(2, 0, 2),
    Vector(4, 2, 0)
  )

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
  // 1. TESTS PARA tIR (Tiempo Inicio Riego)
  // ==========================================

  test("tIR Test 1: Programación secuencial simple (0, 1, 2)") {
    // Orden: T0, luego T1, luego T2.
    // T0 inicia en 0. Dura 3.
    // T1 inicia en 3. Dura 3.
    // T2 inicia en 6. Dura 2.
    // Vector esperado (indices corresponden al id del tablon): Vector(0, 3, 6)
    val pi = Vector(0, 1, 2)
    assert(tIR(fincaPeque, pi) == Vector(0, 3, 6))
  }

  test("tIR Test 2: Programación inversa (2, 1, 0)") {
    // Orden: T2, luego T1, luego T0.
    // T2 inicia en 0. Dura 2.
    // T1 inicia en 2. Dura 3.
    // T0 inicia en 2+3=5. Dura 3.
    // Vector esperado por ID: T0->5, T1->2, T2->0 -> Vector(5, 2, 0)
    val pi = Vector(2, 1, 0)
    assert(tIR(fincaPeque, pi) == Vector(5, 2, 0))
  }

  test("tIR Test 3: Programación del Ejemplo 1 PDF (0, 1, 4, 2, 3)") {
    // Según PDF: tIR debe ser <0, 3, 10, 12, 6>
    // Verifiquemos lógica:
    // T0(tr=3) inicia 0. Fin: 3
    // T1(tr=3) inicia 3. Fin: 6
    // T4(tr=4) inicia 6. Fin: 10
    // T2(tr=2) inicia 10. Fin: 12
    // T3(tr=1) inicia 12. Fin: 13
    // Indices: T0=0, T1=3, T2=10, T3=12, T4=6
    val pi = Vector(0, 1, 4, 2, 3)
    assert(tIR(fincaPDF, pi) == Vector(0, 3, 10, 12, 6))
  }

  test("tIR Test 4: Programación Ejemplo 2 PDF (2, 1, 4, 3, 0)") {
    // Orden: T2, T1, T4, T3, T0
    // T2(tr=2) inicia 0. Fin 2
    // T1(tr=3) inicia 2. Fin 5
    // T4(tr=4) inicia 5. Fin 9
    // T3(tr=1) inicia 9. Fin 10
    // T0(tr=3) inicia 10. Fin 13
    // Vector: T0=10, T1=2, T2=0, T3=9, T4=5
    val pi = Vector(2, 1, 4, 3, 0)
    assert(tIR(fincaPDF, pi) == Vector(10, 2, 0, 9, 5))
  }

  test("tIR Test 5: Finca un solo tablón") {
    val fincaUno = Vector((10, 5, 1))
    val pi = Vector(0)
    assert(tIR(fincaUno, pi) == Vector(0))
  }

  // ==========================================
  // 2. TESTS PARA costoRiegoTablon
  // ==========================================

  test("costoRiegoTablon Test 1: Cultivo sano (Tiempo sobra)") {
    // T0: (10, 3, 4). Si inicia en 0, termina en 3.
    // 10 - 3 = 7. 7 >= 0. Costo: 10 - 3 = 7.
    val pi = Vector(0, 1, 2)
    assert(costoRiegoTablon(0, fincaPeque, pi) == 7)
  }

  test("costoRiegoTablon Test 2: Cultivo al límite (Tiempo exacto)") {
    // T1: (5, 3, 3). Supongamos programación donde inicia en 2.
    // Termina en 2 + 3 = 5.
    // ts(5) - fin(5) = 0. Costo = 0.
    // Forzamos un inicio en 2 simulando una prog ficticia para probar la funcion.
    // Necesitamos que tIR devuelva 2 para el tablon 1.
    // Usamos prog (2, 1, 0) -> T1 inicia en 2 (ver tIR Test 2).
    val pi = Vector(2, 1, 0)
    assert(costoRiegoTablon(1, fincaPeque, pi) == 0)
  }

  test("costoRiegoTablon Test 3: Cultivo muerto con penalización baja") {
    // T2: (2, 2, 1). Supongamos prog (0, 1, 2).
    // T2 inicia en 6 (ver tIR Test 1). Termina en 8.
    // ts(2) - fin(8) = -6. Muere.
    // Costo penalidad: Prio(1) * (fin(8) - ts(2)) = 1 * 6 = 6.
    val pi = Vector(0, 1, 2)
    assert(costoRiegoTablon(2, fincaPeque, pi) == 6)
  }

  test("costoRiegoTablon Test 4: Cultivo muerto con penalización alta") {
    // T0: (10, 3, 4). Supongamos que inicia muy tarde.
    // Usemos prog (2, 1, 4, 3, 0) del ejemplo PDF.
    // T0 inicia en 10. Termina en 13.
    // ts(10) - fin(13) = -3. Muere.
    // Costo: Prio(4) * (13 - 10) = 4 * 3 = 12.
    val pi = Vector(2, 1, 4, 3, 0)
    assert(costoRiegoTablon(0, fincaPDF, pi) == 12)
  }

  test("costoRiegoTablon Test 5: Tablón regado tarde pero prioridad media") {
    // T1: (5, 3, 3). En prog (0, 1, 2) inicia en 3. Fin en 6.
    // ts(5) - fin(6) = -1. Muere.
    // Costo: Prio(3) * (6 - 5) = 3.
    val pi = Vector(0, 1, 2)
    assert(costoRiegoTablon(1, fincaPeque, pi) == 3)
  }

  // ==========================================
  // 3. TESTS PARA costoRiegoFinca
  // ==========================================

  test("costoRiegoFinca Test 1: Suma simple fincaPequeña") {
    // Prog: (0, 1, 2)
    // T0 (inicia 0, fin 3): Sano. Costo 10 - 3 = 7
    // T1 (inicia 3, fin 6): Muerto. Prio 3. Costo 3 * (6-5) = 3
    // T2 (inicia 6, fin 8): Muerto. Prio 1. Costo 1 * (8-2) = 6
    // Total: 7 + 3 + 6 = 16
    val pi = Vector(0, 1, 2)
    assert(costoRiegoFinca(fincaPeque, pi) == 16)
  }

  test("costoRiegoFinca Test 2: Ejemplo 1 del PDF") {
    // Según PDF, para F1 y PI1 (0, 1, 4, 2, 3) el costo de riego es 33.
    // Cálculos PDF: 7 + 3 + 10 + 5 + 8 = 33.
    val pi = Vector(0, 1, 4, 2, 3)
    assert(costoRiegoFinca(fincaPDF, pi) == 33)
  }

  test("costoRiegoFinca Test 3: Ejemplo 2 del PDF") {
    // Según PDF, para F1 y PI2 (2, 1, 4, 3, 0) el costo de riego es 20.
    val pi = Vector(2, 1, 4, 3, 0)
    assert(costoRiegoFinca(fincaPDF, pi) == 20)
  }

  test("costoRiegoFinca Test 4: Todos mueren") {
    // Finca ficticia donde todo es urgente
    val fincaUrgent = Vector((1, 5, 10), (1, 5, 10))
    // T0: (1, 5, 10), T1: (1, 5, 10)
    // Prog (0, 1):
    // T0: Inicia 0, Fin 5. Muere. Costo: 10 * (5-1) = 40.
    // T1: Inicia 5, Fin 10. Muere. Costo: 10 * (10-1) = 90.
    // Total: 130.
    val pi = Vector(0, 1)
    assert(costoRiegoFinca(fincaUrgent, pi) == 130)
  }

  test("costoRiegoFinca Test 5: Todos sobreviven") {
    val fincaRelax = Vector((100, 1, 1), (100, 1, 1))
    // Prog (0, 1):
    // T0: Fin 1. Costo 100 - 1 = 99.
    // T1: Fin 2. Costo 100 - 2 = 98.
    // Total 197.
    val pi = Vector(0, 1)
    assert(costoRiegoFinca(fincaRelax, pi) == 197)
  }

  // ==========================================
  // 4. TESTS PARA costoMovilidad
  // ==========================================

  test("costoMovilidad Test 1: FincaPeque camino (0, 1, 2)") {
    // Camino 0 -> 1 -> 2
    // Dist(0,1) = 2
    // Dist(1,2) = 2
    // Total = 4
    val pi = Vector(0, 1, 2)
    assert(costoMovilidad(fincaPeque, pi, distPeque) == 4)
  }

  test("costoMovilidad Test 2: FincaPeque camino inverso (2, 1, 0)") {
    // Camino 2 -> 1 -> 0
    // Dist(2,1) = 2
    // Dist(1,0) = 2
    // Total = 4
    val pi = Vector(2, 1, 0)
    assert(costoMovilidad(fincaPeque, pi, distPeque) == 4)
  }

  test("costoMovilidad Test 3: Ejemplo 1 PDF") {
    // PI1: (0, 1, 4, 2, 3)
    // 0->1 (2) + 1->4 (6) + 4->2 (2) + 2->3 (2) = 12
    val pi = Vector(0, 1, 4, 2, 3)
    assert(costoMovilidad(fincaPDF, pi, distPDF) == 12)
  }

  test("costoMovilidad Test 4: Ejemplo 2 PDF") {
    // PI2: (2, 1, 4, 3, 0)
    // 2->1 (4) + 1->4 (6) + 4->3 (4) + 3->0 (4) = 18
    val pi = Vector(2, 1, 4, 3, 0)
    assert(costoMovilidad(fincaPDF, pi, distPDF) == 18)
  }

  test("costoMovilidad Test 5: Salto largo") {
    // Prog (0, 2, 1) en FincaPeque
    // 0->2 (4) + 2->1 (2) = 6
    val pi = Vector(0, 2, 1)
    assert(costoMovilidad(fincaPeque, pi, distPeque) == 6)
  }

  // ==========================================
  // 5. TESTS PARA ProgramacionRiegoOptimo
  // ==========================================

  test("Optimo Test 1: Verificar optimo manual en FincaPeque") {
    // Vamos a calcular las 6 permutaciones posibles de FincaPeque manualmente:
    // T0(10,3,4), T1(5,3,3), T2(2,2,1). Dists: 0-1(2), 1-2(2), 0-2(4)

    // 1. (0,1,2): Riego 16 + Mov 4 = 20
    // 2. (0,2,1):
    //    T0(fin 3, ok, cost 7)
    //    T2(fin 5, muerto, cost 1*(5-2)=3)
    //    T1(fin 8, muerto, cost 3*(8-5)=9)
    //    Riego=19. Mov: 0->2(4)+2->1(2)=6. Total=25.
    // 3. (1,0,2):
    //    T1(fin 3, ok, cost 2)
    //    T0(fin 6, ok, cost 4)
    //    T2(fin 8, muerto, cost 1*(8-2)=6)
    //    Riego=12. Mov: 1->0(2)+0->2(4)=6. Total=18.
    // 4. (1,2,0):
    //    T1(fin 3, ok, cost 2)
    //    T2(fin 5, muerto, cost 3)
    //    T0(fin 8, ok, cost 2)
    //    Riego=7. Mov: 1->2(2)+2->0(4)=6. Total=13.
    // 5. (2,0,1):
    //    T2(fin 2, ok, cost 0)
    //    T0(fin 5, ok, cost 5)
    //    T1(fin 8, muerto, cost 9)
    //    Riego=14. Mov: 2->0(4)+0->1(2)=6. Total=20.
    // 6. (2,1,0):
    //    T2(fin 2, ok, cost 0)
    //    T1(fin 5, ok, cost 0)
    //    T0(fin 8, ok, cost 2)
    //    Riego=2. Mov: 2->1(2)+1->0(2)=4. Total=6.

    // EL MÍNIMO ES (2, 1, 0) con costo 6.

    val (prog, costo) = ProgramacionRiegoOptimo(fincaPeque, distPeque)
    assert(prog == Vector(2, 1, 0))
    assert(costo == 6)
  }

  test("Optimo Test 2: Finca PDF vs Ejemplo 1 y 2") {
    // El PDF dice:
    // Prog 1 costo: 45
    // Prog 2 costo: 38
    // El algoritmo debe encontrar algo <= 38.
    val (prog, costo) = ProgramacionRiegoOptimo(fincaPDF, distPDF)
    assert(costo <= 38)
  }

  test("Optimo Test 3: Finca de 1 elemento") {
    val f = Vector((10,2,1))
    val d = Vector(Vector(0))
    val (prog, costo) = ProgramacionRiegoOptimo(f, d)
    // Costo riego: 10 - 2 = 8. Mov: 0. Total 8.
    assert(prog == Vector(0))
    assert(costo == 8)
  }

  test("Optimo Test 4: Prioridad manda") {
    // Dos tablones idénticos en tiempos, pero uno tiene prioridad masiva.
    // T0: (5, 5, 1) -> Si muere paga 1x
    // T1: (5, 5, 1000) -> Si muere paga 1000x
    // Ambos tardan 5 en regarse, sobreviven 5.
    // Si hago (0, 1): T0 ok, T1 muere (costo 1000 * 5 = 5000)
    // Si hago (1, 0): T1 ok, T0 muere (costo 1 * 5 = 5)
    // Debe elegir (1, 0). Distancia asumamos 0 para no interferir.
    val f = Vector((5,5,1), (5,5,1000))
    val d = Vector(Vector(0,0), Vector(0,0))

    val (prog, costo) = ProgramacionRiegoOptimo(f, d)
    assert(prog == Vector(1, 0))
  }

  test("Optimo Test 5: Distancia manda") {
    // T0 y T1 idénticos en todo (no mueren, o penalidad igual).
    // Pero ir 0->1 es carísimo, ir 1->0 es barato (si fuera asimétrico,
    // pero el problema dice simétrico).
    // Hagamos 3 nodos. 0 esta cerca de 1. 2 esta LEJOS.
    // T0, T1, T2 iguales.
    // D: 0-1(1), 1-2(100), 0-2(100).
    // Camino (0,1,2) -> 1 + 100 = 101
    // Camino (2,0,1) -> 100 + 1 = 101
    // Camino (0,2,1) -> 100 + 100 = 200 (Mal)
    // Si T2 es muy urgente, debería ir primero aunque cueste llegar.
    // Caso: T0(100,1,1), T1(100,1,1), T2(100,1,1). Riego siempre bajo. Solo importa mov.
    // Optimo debe minimizar distancia.
    // (0, 1, 2) costo mov 101.
    // (1, 0, 2) costo mov 1 + 100 = 101.
    // (0, 2, 1) costo mov 100 + 100 = 200.
    val f = Vector((100,1,1), (100,1,1), (100,1,1))
    val d = Vector(
      Vector(0, 1, 100),
      Vector(1, 0, 100),
      Vector(100, 100, 0)
    )
    val (prog, costo) = ProgramacionRiegoOptimo(f, d)
    // Esperamos que NO elija el camino que cruza entre los lejanos innecesariamente
    // Costo esperado riego: ~297. Costo mov minimo: 101.
    // Total aprox 398.
    // Verificamos que no sea el camino malo.
    assert(prog != Vector(0, 2, 1) && prog != Vector(1, 2, 0))
  }
}