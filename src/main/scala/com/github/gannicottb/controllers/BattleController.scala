package com.github.gannicottb.controllers
import com.github.gannicottb.model._
import zhttp.http._
import zio.stream._
import zio._

object BattleController {
  def random: ZIO[Clock with Random, Nothing, Response] = for {
    p1 <- Pokemon.random
    p2 <- Pokemon.random
    header = Seq(p1.prettyPrint, "!!--VS--!!", p2.prettyPrint).mkString("\n")
    footer = "The Battle is over!"
    stream = Battle(p1, p2).simulate2.provideEnvironment(ZEnvironment.default)
  } yield Response(
    status = Status.OK,
    data = HttpData.fromStream(
      (Stream(header) ++ stream ++ Stream(footer)).intersperse("\n")
    )
  )
}
