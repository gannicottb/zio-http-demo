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

  def random = zio.Random.nextIntBetween(0, values.length).map(values(_))
}
