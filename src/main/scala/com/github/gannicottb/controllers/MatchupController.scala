package com.github.gannicottb.controllers

import com.github.gannicottb.model.{Matchup, Type}
import zhttp.http.{Request, Response, Status, URL}
import zio.{Task, UIO}

object MatchupController {
  def show(a: String, d: String) = UIO {
    (for {
      attackerType <- Type.ofNameInsensitive(a)
      defenderType <- Type.ofNameInsensitive(d)
    } yield {
      val multiplier = Matchup.compute(attackerType, defenderType)
      Response.text(s"${attackerType.name} is ${multiplier}x against ${defenderType.name}")
    }).getOrElse(Response.status(Status.BAD_REQUEST))
  }
}
