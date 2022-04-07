package com.github.gannicottb.model
import zio.Random._
import zio.Clock._
import java.util.concurrent.TimeUnit
import java.sql.Time

trait BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon)
}
case object Simple extends BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon) = {
    // Step 1: do we have super effective move(s)?
    val superEffective = self.moves.filter(m => Matchup.compute(m.pokeType, opponent.pokeType) == 2.0)

  }
}

case class Stats(attack: Int, specialAttack: Int, defense: Int, specialDefense: Int)
object Stats {
  def random = for {
    a <- nextIntBetween(1, 30)
    sa <- nextIntBetween(1, 30)
    d <- nextIntBetween(1, 20)
    sd <- nextIntBetween(1, 20)
  } yield Stats(a, sa, d, sd)
}

final case class Pokemon(name: String, pokeType: Type, stats: Stats, moves: Set[Move], strategy: BattleStrategy)
object Pokemon {
  def random = for {
    name <- currentTime(TimeUnit.MILLISECONDS).zip(nextInt).map { case (time, i) =>
      s"Mon#${time + i}"
    }
    pokeType <- Type.random
    stats <- Stats.random
    moves <- Move.random
    strategy = Simple
  } yield Pokemon(name, pokeType, stats, moves, strategy)
}
