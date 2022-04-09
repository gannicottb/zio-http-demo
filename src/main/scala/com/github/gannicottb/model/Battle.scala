package com.github.gannicottb.model

import zio.stream._
import scala.annotation.tailrec

case class State(p1Hp: Int, p2Hp: Int, turn: Int = 1)
case class Turn(description: String)
case class Battle(p1: Pokemon, p2: Pokemon) {
  // try tracking HP on the pokemon themselves
  def unfold = ZStream.unfold(State(p1Hp = p1.stats.hp, p2Hp = p2.stats.hp)) { state =>
    val (attacker, defender) = if (state.turn % 2 != 0) p1 -> p2 else p2 -> p1
    val move = attacker.strategy.nextMove(attacker, defender)
    val damageDone = move.calculateDamage(attacker, defender)
    val newState =
      if (attacker == p1) state.copy(p2Hp = state.p2Hp - damageDone)
      else state.copy(p1Hp = state.p1Hp - damageDone)
    println(
      s"Turn ${state.turn}: ${attacker.name} used ${move.name}! It dealt ${damageDone} damage to ${defender.name}"
    )
    if (state.turn > 15 || newState.p1Hp <= 0 || newState.p2Hp <= 0) {
      None
    } else Some((state, newState.copy(turn = newState.turn + 1)))
  }
}
