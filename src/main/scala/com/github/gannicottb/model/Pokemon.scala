package com.github.gannicottb.model
import zio.Random._
import zio.Clock._
import java.util.concurrent.TimeUnit
import java.sql.Time

trait BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon): Move
}
case object Simple extends BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon): Move = {
    // Step 1: do we have super effective move(s)?
    // val superEffective = self.moves.filter(m => Matchup.compute(m.pokeType, opponent.pokeType) == 2.0)
    self.moves.head
  }
}

case class Stats(hp: Int, attack: Int, specialAttack: Int, defense: Int, specialDefense: Int)
object Stats {
  def random = for {
    hp <- nextIntBetween(30, 50)
    a <- nextIntBetween(10, 40)
    sa <- nextIntBetween(10, 40)
    d <- nextIntBetween(10, 30)
    sd <- nextIntBetween(1, 30)
  } yield Stats(hp, a, sa, d, sd)
}

final case class Pokemon(
    name: String,
    pokeType: Type,
    level: Int,
    stats: Stats,
    moves: List[Move],
    strategy: BattleStrategy
)
object Pokemon {
  def random = for {
    name <- currentTime(TimeUnit.MILLISECONDS).zip(nextInt).map { case (time, i) =>
      s"Mon#${time + i}"
    }
    pokeType <- Type.random
    level <- nextIntBetween(5, 10)
    stats <- Stats.random
    moves <- Move.random
    strategy = Simple
  } yield Pokemon(name, pokeType, level, stats, moves, strategy)
}
