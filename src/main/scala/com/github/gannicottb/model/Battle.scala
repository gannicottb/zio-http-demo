package com.github.gannicottb.model

import zio.stream._

/** A Step is a discrete point in the battle encapsulating the outcome of a single move.
  *
  * A Turn is a round of up to two Steps. A Turn can end early if a pokemon faints.
  *
  * Stream[Step]
  */
case class MoveResult(attackerId: Int, move: Move, defenderId: Int, damageDone: Option[Int])
case class Step(battlers: Map[Int, Pokemon], moveResult: Option[MoveResult]) {
  def prettyPrint: String = {
    moveResult
      .map { mr =>
        val attacker = battlers(mr.attackerId)
        val defender = battlers(mr.defenderId)
        Seq(
          s"${attacker.name} used ${mr.move.name}! ${defender.name} took ${mr.damageDone.getOrElse(0)} damage.",
          battlers.values.map(pokemon => s"${pokemon.name}: ${pokemon.currentHP}/${pokemon.stats.hp}").mkString("\n")
        ).mkString("\n")
      }
      .getOrElse("Nothing happened on this step.")
  }
}

case class Battle(pokemon1: Pokemon, pokemon2: Pokemon) {
  val MAX_TURNS = 20

  def simulate: ZStream[Any, Nothing, String] = {
    val firstStep = Step(
      battlers = Map(
        1 -> pokemon1,
        2 -> pokemon2
      ),
      moveResult = None
    )
    ZStream
      .paginate((1, Seq[Step](firstStep))) {
        case (turn, prevSteps) if turn > MAX_TURNS =>
          (turn, prevSteps) -> None // Enforce a turn limit
        case (turn, prevSteps) =>
          val battlers = prevSteps.last.battlers
          val (p1, p2) = (battlers(1), battlers(2))
          println(s"Processing Turn $turn. Pokemon1 has ${p1.currentHP}. Pokemon2 has ${p2.currentHP}")
          val (p1Move, p2Move) = (p1.nextMove(p2), p2.nextMove(p1))
          println(s"p1 will use ${p1Move.name}. p2 will use ${p2Move.name}")
          val inOrder = Seq(
            (1, p1Move, 2),
            (2, p2Move, 1)
          )
          val stepsThisTurn = inOrder.foldLeft(Seq[Step]()) { case (steps, (attackerId, move, defenderId)) =>
            val lastStep = steps.lastOption.getOrElse(prevSteps.last)
            if (lastStep.battlers.values.exists(_.hasFainted)) {
              steps // No more steps if someone's fainted
            } else {
              val damageDone = move.calculateDamage(battlers(attackerId), battlers(defenderId))
              val moveResult = MoveResult(attackerId, move, defenderId, Some(damageDone))
              println(s"Turn $turn: $moveResult")
              val updatedBattlers = lastStep.battlers.updatedWith(defenderId) { p =>
                p.map(_.updateHP(-moveResult.damageDone.getOrElse(0)))
              }
              steps :+ Step(updatedBattlers, Some(moveResult))
            }
          }

          println(
            s"stepsThisTurn: ${stepsThisTurn.map(_.battlers.map { case (index, poke) => s"$index: ${poke.currentHP}" })}"
          )

          if (stepsThisTurn.last.battlers.values.exists(_.hasFainted)) {
            (turn, stepsThisTurn) -> None // this is the last step
          } else {
            (turn, stepsThisTurn) -> Some((turn + 1, stepsThisTurn)) // the battle continues
          }
      }
      .flatMap { case (turn, steps) =>
        Stream(s"Turn $turn:") ++ Stream.fromIterable(steps.map(_.prettyPrint))
      }
  }
}
