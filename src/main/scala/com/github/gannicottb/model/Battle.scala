package com.github.gannicottb.model

import zio.{Random, UIO, ZIO}
import zio.stream._

/** A Battle is between two Pokemon. (for now) Each Pokemon has stats, moves, etc Simulating the battle produces a
  * Stream of Steps But the Battle proceeds in a series of Turns. A Turn has a number and a sequence of Steps
  *
  * During a Turn, Steps are made like this:
  *
  * Each/both pokemon choose their next Move based on the current state (theirs and their foe's) This selection is a
  * Plan, and it contains the attacker, the attacker's Move, and the defender Plans are then resolved in priority order
  * Resolving a Plan means executing its Move and changing the battle state, resulting in a Step.
  *
  * The use of paginate is maybe a little weird, but the idea is that we want to maintain some state while we
  * recursively generate results.
  * @param pokemon1
  * @param pokemon2
  */
case class Turn(number: Int, steps: Seq[Step] = Seq[Step]())
case class Battle(pokemon1: Pokemon, pokemon2: Pokemon) {
  val MAX_TURNS = 20
  def simulate = {
    ZStream.paginateZIO(Turn(1)) {
      case lastTurn @ Turn(number, _) if number >= MAX_TURNS =>
        UIO(lastTurn -> None) // Enforce a turn limit
      case Turn(number, prevSteps) =>
        val (p1, p2) = prevSteps.lastOption.map(s => s.pokemon1 -> s.pokemon2).getOrElse(pokemon1 -> pokemon2)
        for {
          stepsThisTurn <- Plan.resolveAll(Plan.plans(p1, p2))
          nextTurn =
            if (stepsThisTurn.lastOption.exists(_.isOver)) {
              None // the battle is over
            } else {
              Some(Turn(number + 1, stepsThisTurn)) // the battle continues
            }
        } yield Turn(number, stepsThisTurn) -> nextTurn
    }
  }
}

case class Step(pokemon1: Pokemon, pokemon2: Pokemon, move: Move, description: String) {
  def isOver: Boolean = Seq(pokemon1, pokemon2).exists(_.hasFainted)

  def prettyPrint: String = description
}
case class Plan(attacker: Pokemon, move: Move, defender: Pokemon) {
  // Does this plan go first?
  def priority: Int = attacker.stats.speed

  def resolve(prevStep: Option[Step]) = {
    // I think this is leaky - the default only works if we assume that p1 attacks first, right?
    val (p1, p2) = prevStep.map(s => s.pokemon1 -> s.pokemon2).getOrElse(attacker, defender)
    if (prevStep.exists(_.isOver)) UIO.none
    else {
      for {
        step <- move.resolve(p1 -> p2, attacker.id == p1.id)
      } yield Some(step)
    }
  }
}
object Plan {
  // returns Plans in correct order
  def plans(p1: Pokemon, p2: Pokemon) = {
    Seq(p1.nextPlan(p2), p2.nextPlan(p1))
      .sortBy(_.priority)
      .reverse // reverse because default order is ascending
  }

  def resolveAll(plans: Seq[Plan]) =
    ZIO.foldLeft(plans)(Seq[Step]()) { case (steps, plan) =>
      plan.resolve(steps.lastOption).map(_.map(newStep => steps :+ newStep).getOrElse(steps))
    }
}
