package com.github.gannicottb.controllers

import com.github.gannicottb.model.{Matchup, Type}
import zhttp.http.{Request, Response, Status}
import zio.UIO

object TypeController {
  def show(id: String) = UIO {
    Type
      .ofNameInsensitive(id)
      .map { theType =>
        val thisChart = Matchup.chart(theType.name).mkString("\n")
        println(thisChart)
        Response.text(thisChart)
      }
      .getOrElse(Response.status(Status.NOT_FOUND))

  }
}
