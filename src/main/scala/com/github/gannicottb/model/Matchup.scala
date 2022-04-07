package com.github.gannicottb.model

import Type._

/** Represent the matchup between two types.
  * @param attacker
  * @param defender
  * @param multiplier
  */
case class Matchup(attacker: Type, defender: Type, multiplier: Double)
object Matchup {

  // Sparse matrix - default to 1 on a miss
  val chart: Map[Type, Map[Type, Double]] = Map(
    Normal -> Map(
      Rock -> 0.5,
      Ghost -> 0
    ),
    Fight -> Map(
      Normal -> 2,
      Flying -> 0.5,
      Poison -> 0.5,
      Rock -> 2,
      Bug -> 0.5,
      Ghost -> 0,
      Psychic -> 0.5,
      Ice -> 2
    )
    // ...and so on
  )

  def compute(attacker: Type, defender: Type): Double = {
    chart.get(attacker).flatMap { _.get(defender) }.getOrElse(1.0)
  }
}
