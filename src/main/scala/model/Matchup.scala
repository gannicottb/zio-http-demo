package model

/** Represent the matchup between two types.
  * @param attacker
  * @param defender
  * @param multiplier
  */
case class Matchup(attacker: Type, defender: Type, multiplier: Double)
object Matchup {

  // Sparse matrix - default to 1 on a miss
  val chart: Map[String, Map[String, Double]] = Map(
    "normal" -> Map(
      "rock" -> 0.5,
      "ghost" -> 0
    ),
    "fight" -> Map(
      "normal" -> 2,
      "flying" -> 0.5,
      "poison" -> 0.5,
      "rock" -> 2,
      "bug" -> 0.5,
      "ghost" -> 0,
      "psychic" -> 0.5,
      "ice" -> 2
    )
    // ...and so on
  )

  def compute(attacker: Type, defender: Type): Double =
    chart.get(attacker.name.toLowerCase).flatMap { _.get(defender.name.toLowerCase) }.getOrElse(1.0)
}
