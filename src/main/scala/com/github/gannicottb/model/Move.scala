package com.github.gannicottb.model
import Type._

import enumeratum._
import com.github.gannicottb.model.MoveCategory.Physical
import com.github.gannicottb.model.MoveCategory.Special
import zio.ZIO
import zio.Random._
import zio.UIO
import zio.Random

sealed trait MoveCategory extends EnumEntry
object MoveCategory extends Enum[MoveCategory] {
  val values = findValues
  case object Physical extends MoveCategory
  case object Special extends MoveCategory
}

case class Move(name: String, pokeType: Type, category: MoveCategory, power: Int, accuracy: Double, pp: Int) {
  // def calculateDamage(self: Pokemon, opponent: Pokemon): Int = {
  //   val levelComponent = ((2 * self.level / 5) + 2)
  //   val attackDefenseComparison = category match {
  //     case Physical => self.stats.attack / opponent.stats.defense
  //     case Special  => self.stats.specialAttack / opponent.stats.specialDefense
  //   }
  //   val STAB: Double = if (pokeType == self.pokeType) 1.5 else 1.0
  //   val effectiveness: Double = Matchup.compute(self.pokeType, opponent.pokeType)

  //   (((levelComponent * power * attackDefenseComparison) / 50 + 2).toDouble * STAB * effectiveness).toInt
  // }

  def calculateOutcome(self: Pokemon, opponent: Pokemon): ZIO[Random, NoSuchElementException, Outcome] = {
    for {
      doesHit <- nextDouble.map(_ <= accuracy)
      (damage, description) <-
        if (!doesHit) UIO(None -> Some("The attack missed!"))
        else {
          nextDoubleBetween(.85, 1.0).map { randomFactor =>
            val levelComponent = ((2 * self.level / 5) + 2)
            val attackDefenseComparison = category match {
              case Physical => self.stats.attack / opponent.stats.defense
              case Special  => self.stats.specialAttack / opponent.stats.specialDefense
            }
            val STAB: Double = if (pokeType == self.pokeType) 1.5 else 1.0
            val effectiveness: Double = Matchup.compute(self.pokeType, opponent.pokeType)
            val baseDamage = ((levelComponent * power * attackDefenseComparison) / 50 + 2)

            val damage =
              Some((baseDamage.toDouble * STAB * effectiveness * randomFactor).toInt)
            val description = effectiveness match {
              case 2.0 => Some("It was super effective!")
              case 0.5 => Some("It's not every effective...")
              case _   => None
            }
            damage -> description
          }
        }
    } yield Outcome(
      self,
      opponent,
      damage,
      description
    )
  }

}

object Move {
  import MoveCategory._

  // TODO: enable custom logic for moves (to apply status effects, override damage/priority, etc)
  val default = List(
    Move("Tackle", Normal, Physical, power = 40, accuracy = 1.0, pp = 35),
    Move("Karate Chop", Fight, Physical, power = 50, accuracy = 1.0, pp = 25),
    Move("Peck", Flying, Physical, power = 35, accuracy = 1.0, pp = 35),
    Move("Acid", Poison, Physical, power = 40, accuracy = 1.0, pp = 30),
    Move("Bone Club", Ground, Physical, power = 65, accuracy = .85, pp = 20),
    Move("Rock Throw", Rock, Physical, power = 50, accuracy = .9, pp = 15),
    Move("Leech Life", Bug, Physical, power = 80, accuracy = 1.0, pp = 10),
    Move("Lick", Ghost, Physical, power = 30, accuracy = 1.0, pp = 30),
    Move("Ember", Fire, Special, power = 40, accuracy = 1.0, pp = 25),
    Move("Bubble", Water, Special, power = 40, accuracy = 1.0, pp = 30),
    Move("Vine Whip", Grass, Physical, power = 45, accuracy = 1.0, pp = 25),
    Move("Thunder Shock", Electric, Special, power = 40, accuracy = 1.0, pp = 30),
    Move("Psybeam", Psychic, Special, power = 65, accuracy = 1.0, pp = 20),
    Move("Aurora Beam", Ice, Special, power = 65, accuracy = 1.0, pp = 20),
    Move("Dragon Rage", Dragon, Special, power = 40, accuracy = 1.0, pp = 10)
  )

  def random = zio.Random.shuffle(default).map(_.take(1))
}
