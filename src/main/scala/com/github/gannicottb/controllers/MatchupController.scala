package com.github.gannicottb.controllers

import com.github.gannicottb.model.{Matchup, Type}
import zhttp.http.{Request, Response, Status, URL}
import zio.{Task, UIO}

object MatchupController {
  def show(req: Request) = UIO {
    (for {
      attackerType <- parseTypeFromParam(req.url)("attacker")
      defenderType <- parseTypeFromParam(req.url)("defender")
    } yield {
      val multiplier = Matchup.compute(attackerType, defenderType)
      Response.text(s"${attackerType.name} is ${multiplier}x against ${defenderType.name}")
    }).getOrElse(Response.status(Status.BAD_REQUEST))
  }

  private def parseTypeFromParam(url: URL)(key: String): Option[Type] = for {
    // Get all of the params for the two keys we care about
    params <- url.queryParams.get(key)
    // Just take the first value
    firstParam <- params.headOption
    // Then look up the value to get the Type
    theType <- Type.ofNameInsensitive(firstParam)
  } yield theType
}
