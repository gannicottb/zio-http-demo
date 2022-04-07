package com.github.gannicottb.model
import zio.Random._
import zio.Clock._
import java.util.concurrent.TimeUnit

trait BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon)
}
case object Simple extends BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon) = {}
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

final case class Pokemon(name: String, stats: Stats, moves: Set[Move], strategy: BattleStrategy)
object Pokemon {
  def random = for {
    randomIndex <- nextInt
    name <- currentTime(TimeUnit.MILLISECONDS).map(t => s"Mon#${t + randomIndex}")
    stats <- Stats.random
    moves <- Move.random
    strategy = Simple
  } yield Pokemon(name, stats, moves, strategy)
}
