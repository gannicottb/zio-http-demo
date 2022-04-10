package com.github.gannicottb.controllers

import com.github.gannicottb.model.{Matchup, Type}
import zhttp.http.{Request, Response, Status}
import zio.UIO

object TypeController {
  def show(id: String) = UIO {
    Type
      .withNameInsensitiveOption(id)
      .map { attacker =>
        val thisChart =
          Type.values
            .map { defender =>
              val multiplier = Matchup.compute(attacker, defender)
              s"$attacker vs $defender: ${formatMultiplier(multiplier)}"
            }
            .mkString("\n")
        Response.text(thisChart)
      }
      .getOrElse(Response.status(Status.NOT_FOUND))

  }

  private def formatMultiplier(multiplier: Double) = multiplier match {
    case 2.0 => "[2x]"
    case .5  => "1/2x"
    case 0   => "0x"
    case _   => "1x"
  }
}
