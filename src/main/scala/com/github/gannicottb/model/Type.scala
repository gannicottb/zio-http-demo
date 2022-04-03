package com.github.gannicottb.model

/** Model the Pokemon types. The generation should point to
  * @param name
  */
case class Type(id: Int, name: String, generation: Generation)
object Type {

  val allTypes = Seq(
    "normal",
    "fight",
    "flying",
    "poison",
    "ground",
    "rock",
    "bug",
    "ghost",
    "fire",
    "water",
    "grass",
    "electric",
    "psychic",
    "ice",
    "dragon"
  )

  /** In memory store for now
    */
  val store: Map[String, Type] = allTypes.zipWithIndex.foldLeft(Map[String, Type]()) { case (map, (name, index)) =>
    map + (name -> Type(index, name, Generation(1, "I")))
  }
  def ofNameInsensitive(name: String): Option[Type] = store.get(name.toLowerCase)
}
