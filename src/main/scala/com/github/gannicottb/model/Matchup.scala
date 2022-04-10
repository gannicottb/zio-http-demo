package com.github.gannicottb.model

import Type._

/** Represent the matchup between two types.
  * @param attacker
  * @param defender
  * @param multiplier
  */
case class Matchup(attacker: Type, defender: Type, multiplier: Double)
object Matchup {
  // source: https://bulbapedia.bulbagarden.net/wiki/Type/Type_chart#Generation_I
  def compactChart(attacker: Type, defender: Type) = {
    (attacker, defender) match {
      case (Normal, Ghost)                                        => 0.0
      case (Normal, Rock)                                         => .5
      case (Fight, Ghost)                                         => 0
      case (Fight, Flying | Poison | Bug | Psychic)               => .5
      case (Fight, Normal | Rock | Ice)                           => 2.0
      case (Flying, Rock | Electric)                              => .5
      case (Flying, Fight | Bug | Grass)                          => 2.0
      case (Poison, Poison | Ground | Rock | Ghost)               => .5
      case (Poison, Bug | Grass)                                  => 2.0
      case (Ground, Flying)                                       => 0.0
      case (Ground, Bug | Grass)                                  => .5
      case (Ground, Poison | Rock | Fire | Electric)              => 2.0
      case (Rock, Fight | Ground)                                 => .5
      case (Rock, Flying | Bug | Fire | Ice)                      => 2.0
      case (Bug, Fight | Flying | Ghost | Fire)                   => .5
      case (Bug, Poison | Grass | Psychic)                        => 2.0
      case (Ghost, Normal | Psychic)                              => 0.0
      case (Ghost, Ghost)                                         => 2.0
      case (Fire, Rock | Fire | Water)                            => .5
      case (Fire, Bug | Grass | Ice)                              => 2.0
      case (Water, Ground | Rock | Fire)                          => .5
      case (Water, Water | Grass | Dragon)                        => 2.0
      case (Grass, Flying | Poison | Bug | Fire | Grass | Dragon) => .5
      case (Grass, Ground | Rock | Water)                         => 2.0
      case (Electric, Ground)                                     => 0.0
      case (Electric, Grass | Electric | Dragon)                  => .5
      case (Electric, Flying | Water)                             => 2.0
      case (Psychic, Psychic)                                     => .5
      case (Psychic, Fight | Poison)                              => 2.0
      case (Ice, Water | Ice)                                     => .5
      case (Ice, Flying | Ground | Grass | Dragon)                => 2.0
      case (Dragon, Dragon)                                       => 2.0
      case _                                                      => 1.0
    }
  }

  def compute(attacker: Type, defender: Type): Double = {
    compactChart(attacker, defender)
  }
}
