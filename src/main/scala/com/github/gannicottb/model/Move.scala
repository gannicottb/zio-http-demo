package com.github.gannicottb.model
import com.github.gannicottb.model.Type._
import enumeratum._
import zio.Random._
import zio.UIO

sealed trait MoveCategory extends EnumEntry
object MoveCategory extends Enum[MoveCategory] {
  val values = findValues
  case object Physical extends MoveCategory
  case object Special extends MoveCategory
}

final case class Move(name: String, pokeType: Type, category: MoveCategory, power: Int, accuracy: Double, pp: Int) {
  import MoveCategory._

  // For a tuple of battling Pokemon and a boolean telling us which is which, generate the next Step
  def resolve(battlers: (Pokemon, Pokemon), p1Attacking: Boolean) = {
    // Assume that the battlers are provided in p1/p2 order
    // Swap them if needed (and then swap again at the end)
    def normalize(tup: (Pokemon, Pokemon)) = if (p1Attacking) tup else tup.swap

    val (self, foe) = normalize(battlers)
    val description = Seq(s"${self.name} used $name!")
    for {
      doesHit <- nextDouble.map(_ <= accuracy)
      step <-
        if (doesHit) {
          nextDoubleBetween(.85, 1.0).map { randomFactor =>
            val levelComponent: Int = (2 * self.level / 5) + 2
            val attackDefenseComparison: Int = category match {
              case Physical => self.stats.attack / foe.stats.defense
              case Special  => self.stats.specialAttack / foe.stats.specialDefense
            }
            val STAB: Double = if (pokeType == self.pokeType) 1.5 else 1.0
            val effectiveness: Double = Matchup.compute(self.pokeType, foe.pokeType)
            val baseDamage = ((levelComponent * power * attackDefenseComparison) / 50 + 2)

            val changeToFoe =
              Some((-baseDamage.toDouble * STAB * effectiveness * randomFactor).toInt)
            val changeToSelf = None // for now (Take Down, Leech Life)

            // Update the pokemon
            val battlersAfterDamage = (
              self.updateHP(changeToSelf.getOrElse(0)),
              foe.updateHP(changeToFoe.getOrElse(0))
            )
            // Assemble the recap
            val criticalMsg = None // for now
            val effectiveMsg = effectiveness match {
              case 2.0 => Some("It was super effective!")
              case 0.5 => Some("It's not very effective...")
              case _   => None
            }
            val foeDamageMsg = changeToFoe.map(c =>
              if (c < 0) s"${foe.name} took ${-changeToFoe.getOrElse(0)} damage."
              else s"${foe.name} gained ${changeToFoe.getOrElse(0)} HP!"
            )
            val (newP1, newP2) = normalize(battlersAfterDamage)
            val hpMsg = Seq(newP1, newP2)
              .map(pokemon => s"${pokemon.name}: ${pokemon.currentHP}/${pokemon.stats.hp}")
              .mkString("\t||\t")
            val finalDescription = description ++ Seq(
              effectiveMsg,
              criticalMsg,
              foeDamageMsg
            ).collect { case Some(msg) => msg } ++
              Seq("-" * 10, hpMsg, "-" * 10)

            Step(newP1, newP2, this, finalDescription.mkString("\n"))
          }
        } else {
          val finalDescription = (description :+ "The attack missed!").mkString("\n")
          UIO(Step(battlers._1, battlers._2, this, finalDescription))
        }
    } yield step
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
