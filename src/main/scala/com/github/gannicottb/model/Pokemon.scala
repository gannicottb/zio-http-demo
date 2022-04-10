package com.github.gannicottb.model
import zio.Clock._
import zio.Random._

import java.util.concurrent.TimeUnit

trait BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon): Move
}
case object Simple extends BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon): Move = {
    self.moves.head // I did say it was simple...
  }
}

final case class Stats(hp: Int, attack: Int, specialAttack: Int, defense: Int, specialDefense: Int, speed: Int) {
  def prettyPrint = Seq(
    s"HP: $hp",
    s"Attack: $attack",
    s"Defense: $defense",
    s"Sp. Atk: $specialAttack",
    s"Sp. Def: $specialDefense",
    s"Speed: $speed"
  ).mkString("\n")
}
object Stats {
  def random = for {
    hp <- nextIntBetween(30, 50)
    a <- nextIntBetween(10, 40)
    sa <- nextIntBetween(10, 40)
    d <- nextIntBetween(10, 30)
    sd <- nextIntBetween(1, 30)
    spd <- nextIntBetween(30, 50)
  } yield Stats(hp, a, sa, d, sd, spd)
}

final case class Pokemon(
    id: Long,
    name: String,
    pokeType: Type,
    level: Int,
    stats: Stats,
    moves: List[Move],
    strategy: BattleStrategy,
    currentHP: Int
    // status (sleep, confused, burn, freeze, etc)
) {
  // Return a copy of this Pokemon with full HP
  def atMaxHP: Pokemon = copy(currentHP = stats.hp)

  def updateHP(change: Int): Pokemon = copy(currentHP = currentHP + change)

  // Check to see if we've fainted
  def hasFainted: Boolean = currentHP <= 0

  def nextMove(foe: Pokemon): Move = strategy.nextMove(this, foe)

  def nextPlan(foe: Pokemon): Plan = Plan(this, nextMove(foe), foe)

  def prettyPrint = Seq(
    s"$name | Lv.$level ${pokeType.entryName}-type",
    "Stats:",
    stats.prettyPrint,
    "Moves:",
    moves.map(_.name)
  ).mkString("\n")
}
object Pokemon {
  def random = for {
    id <- currentTime(TimeUnit.MILLISECONDS).zip(nextInt).map { case (time, i) =>
      (time + i) / 100_000_000
    }
    name <- Name.random.map(_ + s"#$id")
    pokeType <- Type.random
    level <- nextIntBetween(5, 10)
    stats <- Stats.random
    moves <- Move.random
    strategy = Simple
  } yield Pokemon(id, name, pokeType, level, stats, moves, strategy, stats.hp)
}
