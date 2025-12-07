package taller

import org.scalameter._
import RiegoOptimo._
import RiegoParalelo._

object BenchmarkRiego {

//aqui se pueden cambiar los tamaños de las fincas a probar
  val tamanos = Vector(6, 7, 8, 9, 10)

  def medirTiempo[A](bloque: => A): Double = {
    val t = measure { bloque }
    t.value
  }

  def main(args: Array[String]): Unit = {
    println("========================================")
    println("     BENCHMARK RIEGO ÓPTIMO (Scala)     ")
    println("========================================\n")

    tamanos.foreach { n =>
      println(s"--- TAMAÑO DE FINCA: $n TABLONES ---")

      val finca = fincaAlAzar(n)
      val dist  = distanciaAlAzar(n)

      // Medición secuencial
      val tiempoSec = medirTiempo {
        ProgramacionRiegoOptimo(finca, dist)
      }

      // Medición paralela
      val tiempoPar = medirTiempo {
        ProgramacionRiegoOptimoPar(finca, dist)
      }

      val aceleracion =
        ((tiempoSec - tiempoPar) / tiempoSec) * 100.0

      println(f"Secuencial: $tiempoSec%.2f ms")
      println(f"Paralelo:   $tiempoPar%.2f ms")
      println(f"Aceleración: $aceleracion%.2f %%\n")
    }

    println("========================================")
    println("           FIN DEL BENCHMARK            ")
    println("========================================")
  }
}
