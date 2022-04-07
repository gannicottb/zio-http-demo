package com.github.gannicottb.controllers
import com.github.gannicottb.model._
import com.github.gannicottb.model.Type._
import enumeratum._
import zhttp.http.{Request, Response, Status, URL}
import zio.{Task, UIO}

object MatchupController {
  def show(a: String, d: String) = UIO {
    (for {
      attackerType <- Type.withNameInsensitiveOption(a)
      defenderType <- Type.withNameInsensitiveOption(d)
    } yield {
      val multiplier = Matchup.compute(attackerType, defenderType)
      Response.text(s"${attackerType.entryName} is ${multiplier}x against ${defenderType.entryName}")
    }).getOrElse(Response.status(Status.BAD_REQUEST))
  }
}
