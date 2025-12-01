package taller

import org.scalatest.funsuite.AnyFunSuite

class RiegoParaleloTest extends AnyFunSuite {
import RiegoOptimo._
import RiegoParalelo._

test("ejemploPDF: Programacion optima coincide (F1)") {
val F1: Finca = Vector(
(10, 3, 4),
(5, 3, 3),
(2, 2, 1),
(8, 1, 1),
(6, 4, 2)
)

```
val DF1: Distancia = Vector(
  Vector(0, 2, 2, 4, 4),
  Vector(2, 0, 4, 2, 6),
  Vector(2, 4, 0, 2, 2),
  Vector(4, 2, 2, 0, 4),
  Vector(4, 6, 2, 4, 0)
)

val sec = RiegoOptimo.ProgramacionRiegoOptimo(F1, DF1)
val par = RiegoParalelo.ProgramacionRiegoOptimo(F1, DF1)

// Los costos deben coincidir y la programación óptima (costo mínimo) también
assert(sec._2 == par._2)
```

}

test("aleatorio pequeño: paralelo y secuencial devuelven mismo costo") {
// probamos con n = 4 para mantener tiempo razonable en tests
val n = 4
val f = RiegoOptimo.fincaAlAzar(n)
val d = RiegoOptimo.distanciaAlAzar(n)

```
val sec = RiegoOptimo.ProgramacionRiegoOptimo(f, d)
val par = RiegoParalelo.ProgramacionRiegoOptimo(f, d)

assert(sec._2 == par._2)
```

}

test("caso trivial: finca vacía") {
val f: Finca = Vector.empty
val d: Distancia = Vector.empty
val res = RiegoParalelo.ProgramacionRiegoOptimo(f, d)
assert(res._1.isEmpty)
assert(res._2 == Int.MaxValue)
}

test("caso 1 elemento") {
val f: Finca = Vector((5,2,1))
val d: Distancia = Vector(Vector(0))
val sec = RiegoOptimo.ProgramacionRiegoOptimo(f, d)
val par = RiegoParalelo.ProgramacionRiegoOptimo(f, d)
assert(sec._2 == par._2)
assert(sec._1 == par._1)
}
}
