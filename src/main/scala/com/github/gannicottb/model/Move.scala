package com.github.gannicottb.model
import Type._

import enumeratum._
import com.github.gannicottb.model.MoveCategory.Physical
import com.github.gannicottb.model.MoveCategory.Special

sealed trait MoveCategory extends EnumEntry
object MoveCategory extends Enum[MoveCategory] {
  val values = findValues
  case object Physical extends MoveCategory
  case object Special extends MoveCategory
}

case class Move(name: String, pokeType: Type, power: Int, accuracy: Double, pp: Int, category: MoveCategory) {
  def calculateDamage(self: Pokemon, opponent: Pokemon): Int = {
    val levelComponent = ((2 * self.level / 5) + 2)
    val attackDefenseComparison = category match {
      case Physical => self.stats.attack / opponent.stats.defense
      case Special  => self.stats.specialAttack / opponent.stats.specialDefense
    }

    (levelComponent * power * attackDefenseComparison) / 50 + 2
  }
}

object Move {
  import MoveCategory._

  val default = List(
    Move(name = "Tackle", pokeType = Normal, category = Physical, power = 40, accuracy = 1.0, pp = 35),
    Move(name = "Ember", pokeType = Fire, category = Special, power = 40, accuracy = 1.0, pp = 25),
    Move(name = "Bubble", pokeType = Water, category = Special, power = 40, accuracy = 1.0, pp = 30),
    Move(name = "Karate Chop", pokeType = Fight, category = Physical, power = 50, accuracy = 1.0, pp = 25)
  )

  def random = zio.Random.shuffle(default).map(_.take(1))
}
