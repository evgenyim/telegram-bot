package lab_2

import scala.util.Random

trait Rand {
  def randElem[T](l: List[T]): T
}

object Randomizer extends Rand {
  override def randElem[T](l: List[T]): T = Random.shuffle(l).head
}