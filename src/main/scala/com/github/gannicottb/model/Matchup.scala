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
  // source: https://bulbapedia.bulbagarden.net/wiki/Type/Type_chart#Generation_I
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
    ),
    Flying -> Map(
      Fight -> 2,
      Rock -> .5,
      Bug -> 2,
      Grass -> 2,
      Electric -> .5
    ),
    Poison -> Map(
      Poison -> 0.5,
      Ground -> 0.5,
      Rock -> 0.5,
      Bug -> 2.0,
      Ghost -> 0.5,
      Grass -> 2.0
    ),
    Ground -> Map(
      Flying -> 0,
      Poison -> 2.0,
      Rock -> 2.0,
      Bug -> 0.5,
      Fire -> 2.0,
      Grass -> .5,
      Electric -> 2.0
    ),
    Rock -> Map(),
    Bug -> Map(),
    Ghost -> Map(),
    Fire -> Map(),
    Water -> Map(),
    Grass -> Map(),
    Electric -> Map(),
    Psychic -> Map(),
    Ice -> Map(),
    Dragon -> Map()
  )

  def compute(attacker: Type, defender: Type): Double = {
    chart.get(attacker).flatMap { _.get(defender) }.getOrElse(1.0)
  }
}
