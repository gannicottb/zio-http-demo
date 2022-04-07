package com.github.gannicottb.model

trait BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon)
}
object Simple extends BattleStrategy {
  def nextMove(self: Pokemon, opponent: Pokemon) = {}
}

case class Stats(attack: Int, defense: Int)

final case class Pokemon(strategy: BattleStrategy, stats: Stats)
