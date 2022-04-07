package com.github.gannicottb.model
import Type._

import enumeratum._

sealed trait MoveCategory extends EnumEntry
object MoveCategory extends Enum[MoveCategory] {
  val values = findValues
  case object Physical extends MoveCategory
  case object Special extends MoveCategory
}

case class Move(name: String, pokeType: Type, power: Int, accuracy: Double, pp: Int, category: MoveCategory)

object Move {
  import MoveCategory._

  val default = Set(
    Move(name = "Tackle", pokeType = Normal, category = Physical, power = 40, accuracy = 1.0, pp = 35),
    Move(name = "Ember", pokeType = Fire, category = Special, power = 40, accuracy = 1.0, pp = 25),
    Move(name = "Bubble", pokeType = Water, category = Special, power = 40, accuracy = 1.0, pp = 30),
    Move(name = "Karate Chop", pokeType = Fight, category = Physical, power = 50, accuracy = 1.0, pp = 25)
  )

  def random = zio.Random.nextIntBetween(0, default.size).map { i =>
    default.slice(i, i + 1)
  }
}
