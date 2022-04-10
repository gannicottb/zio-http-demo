package com.github.gannicottb.model

import zio.stream._
import zio.UIO
import zio.ZIO

case class Outcome(attacker: Pokemon, defender: Pokemon, damageDone: Option[Int], description: Option[String])
case class Step2(pokemon1: Pokemon, pokemon2: Pokemon, move: Move, outcome: Outcome) {
  def isOver = Seq(pokemon1, pokemon2).exists(_.hasFainted)

  def prettyPrint: String = {
    val attacker = outcome.attacker
    val defender = outcome.defender
    val lines = Seq(s"${attacker.name} used ${move.name}!") ++
      outcome.description.map(Seq(_)).getOrElse(Seq()) ++
      Seq(s"${defender.name} took ${outcome.damageDone.getOrElse(0)} damage.") ++
      Seq(pokemon1, pokemon2)
        .map(pokemon => s"${pokemon.name}: ${pokemon.currentHP}/${pokemon.stats.hp}")
    lines.mkString("\n")
  }
}
case class Plan(attacker: Pokemon, move: Move, defender: Pokemon) {
  def priority = attacker.stats.speed

  // resolving a plan results in a new step
  def resolve(prevStep: Option[Step2]) = {
    val (p1, p2) = prevStep.map(s => s.pokemon1 -> s.pokemon2).getOrElse(attacker, defender)
    if (prevStep.map(_.isOver).getOrElse(false)) UIO(None)
    else {
      for {
        outcome <- move.calculateOutcome(attacker, defender)
        // This bit could/should be done in outcome
        damageDone = outcome.damageDone.getOrElse(0)
        damageDoneToP1 = if (p1 == defender) damageDone else 0
        damageDoneToP2 = if (p2 == defender) damageDone else 0
      } yield Some(
        Step2(
          pokemon1 = p1.updateHP(-damageDoneToP1),
          pokemon2 = p2.updateHP(-damageDoneToP2),
          move = move,
          outcome = outcome
        )
      )
    }
  }
}
object Plan {
  // def inInitiativeOrder(plans: Seq[Plan]) = {
  //   plans.sortBy(_.priority).reverse // reverse because default order is ascending
  // }
  // returns Plans in correct order
  def plans(p1: Pokemon, p2: Pokemon) = {
    Seq(p1.nextPlan(p2), p2.nextPlan(p1))
      .sortBy(_.priority)
      .reverse // reverse because default order is ascending
  }

  def resolveAll(plans: Seq[Plan]) =
    ZIO.foldLeft(plans)(Seq[Step2]()) { case (steps, plan) =>
      plan.resolve(steps.lastOption).map(_.map(newStep => steps :+ newStep).getOrElse(steps))
    }
}

/** A Step is a discrete point in the battle encapsulating the outcome of a single move.
  *
  * A Turn is a round of up to two Steps. A Turn can end early if a pokemon faints.
  *
  * Stream[Step]
  */
// case class MoveResult(attackerId: Int, move: Move, defenderId: Int, damageDone: Option[Int])
// case class Step(battlers: Map[Int, Pokemon], moveResult: Option[MoveResult]) {
//   def prettyPrint: String = {
//     moveResult
//       .map { mr =>
//         val attacker = battlers(mr.attackerId)
//         val defender = battlers(mr.defenderId)
//         Seq(
//           s"${attacker.name} used ${mr.move.name}! ${defender.name} took ${mr.damageDone.getOrElse(0)} damage.",
//           battlers.values.map(pokemon => s"${pokemon.name}: ${pokemon.currentHP}/${pokemon.stats.hp}").mkString("\n")
//         ).mkString("\n")
//       }
//       .getOrElse("Nothing happened on this step.")
//   }
// }

case class Battle(pokemon1: Pokemon, pokemon2: Pokemon) {
  val MAX_TURNS = 20

  def simulate2 = {
    ZStream.paginateZIO((1, Seq[Step2]())) {
      case (turn, prevSteps) if turn > MAX_TURNS =>
        UIO((turn, prevSteps) -> None) // Enforce a turn limit
      case (turn, prevSteps) =>
        val (p1, p2) = prevSteps.lastOption.map(s => s.pokemon1 -> s.pokemon2).getOrElse(pokemon1 -> pokemon2)
        for {
          stepsThisTurn <- Plan.resolveAll(Plan.plans(p1, p2))
          nextTurn =
            if (stepsThisTurn.lastOption.map(_.isOver).getOrElse(false)) {
              None // the battle is over
            } else {
              Some((turn + 1, stepsThisTurn)) // the battle continues
            }
        } yield (turn, stepsThisTurn) -> nextTurn
    }
  }.flatMap { case (turn, steps) =>
    Stream(s"Turn $turn:") ++ Stream.fromIterable(steps.map(_.prettyPrint))
  }

  // def simulate: ZStream[Any, Nothing, String] = {
  //   val firstStep = Step(
  //     battlers = Map(
  //       1 -> pokemon1,
  //       2 -> pokemon2
  //     ),
  //     moveResult = None
  //   )
  //   ZStream
  //     .paginate((1, Seq[Step](firstStep))) {
  //       case (turn, prevSteps) if turn > MAX_TURNS =>
  //         (turn, prevSteps) -> None // Enforce a turn limit
  //       case (turn, prevSteps) =>
  //         val battlers = prevSteps.last.battlers
  //         val (p1, p2) = (battlers(1), battlers(2))
  //         println(s"Processing Turn $turn. Pokemon1 has ${p1.currentHP}. Pokemon2 has ${p2.currentHP}")
  //         val (p1Move, p2Move) = (p1.nextMove(p2), p2.nextMove(p1))
  //         println(s"p1 will use ${p1Move.name}. p2 will use ${p2Move.name}")
  //         val inOrder = Seq(
  //           (1, p1Move, 2),
  //           (2, p2Move, 1)
  //         )
  //         val stepsThisTurn = inOrder.foldLeft(Seq[Step]()) { case (steps, (attackerId, move, defenderId)) =>
  //           val lastStep = steps.lastOption.getOrElse(prevSteps.last)
  //           if (lastStep.battlers.values.exists(_.hasFainted)) {
  //             steps // No more steps if someone's fainted
  //           } else {
  //             val damageDone = move.calculateDamage(battlers(attackerId), battlers(defenderId))
  //             val moveResult = MoveResult(attackerId, move, defenderId, Some(damageDone))
  //             println(s"Turn $turn: $moveResult")
  //             val updatedBattlers = lastStep.battlers.updatedWith(defenderId) { p =>
  //               p.map(_.updateHP(-moveResult.damageDone.getOrElse(0)))
  //             }
  //             steps :+ Step(updatedBattlers, Some(moveResult))
  //           }
  //         }

  //         println(
  //           s"stepsThisTurn: ${stepsThisTurn.map(_.battlers.map { case (index, poke) => s"$index: ${poke.currentHP}" })}"
  //         )

  //         if (stepsThisTurn.last.battlers.values.exists(_.hasFainted)) {
  //           (turn, stepsThisTurn) -> None // this is the last step
  //         } else {
  //           (turn, stepsThisTurn) -> Some((turn + 1, stepsThisTurn)) // the battle continues
  //         }
  //     }
  //     .flatMap { case (turn, steps) =>
  //       Stream(s"Turn $turn:") ++ Stream.fromIterable(steps.map(_.prettyPrint))
  //     }
  // }
}
