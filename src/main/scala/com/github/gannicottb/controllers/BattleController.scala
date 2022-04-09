package com.github.gannicottb.controllers
import com.github.gannicottb.model._
import zhttp.http._
import zio.stream._

object BattleController {
  def random = for {
    p1 <- Pokemon.random
    p2 <- Pokemon.random
    header = Seq(p1.toString, "!!--VS--!!", p2.toString).mkString("\n")
    footer = "The Battle is over!"
    stream = Battle(p1, p2).simulate
  } yield Response(
    status = Status.OK,
    data = HttpData.fromStream(
      (Stream(header) ++ stream ++ Stream(footer)).intersperse("\n")
    )
  )
}
