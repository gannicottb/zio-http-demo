package com.github.gannicottb.model

import enumeratum._

sealed trait Type extends EnumEntry
object Type extends Enum[Type] {
  val values = findValues

  case object Normal extends Type
  case object Fight extends Type
  case object Flying extends Type
  case object Poison extends Type
  case object Ground extends Type
  case object Rock extends Type
  case object Bug extends Type
  case object Ghost extends Type
  case object Fire extends Type
  case object Water extends Type
  case object Grass extends Type
  case object Electric extends Type
  case object Psychic extends Type
  case object Ice extends Type
  case object Dragon extends Type
}

// /** Model the Pokemon types. The generation should point to
//   * @param name
//   */
// case class Type(id: Int, name: String, generation: Generation)
// object Type {

//   val allTypes = Seq(
//     "normal",
//     "fight",
//     "flying",
//     "poison",
//     "ground",
//     "rock",
//     "bug",
//     "ghost",
//     "fire",
//     "water",
//     "grass",
//     "electric",
//     "psychic",
//     "ice",
//     "dragon"
//   )

//   /** In memory store for now
//     */
//   val store: Map[String, Type] = allTypes.zipWithIndex.foldLeft(Map[String, Type]()) { case (map, (name, index)) =>
//     map + (name -> Type(index, name, Generation(1, "I")))
//   }
//   def ofNameInsensitive(name: String): Option[Type] = store.get(name.toLowerCase)
// }
