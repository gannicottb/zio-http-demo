package com.github.gannicottb.controllers
import com.github.gannicottb.model._
import zhttp.http._
import zio.stream._

object BattleController {
  def random = for {
    p1 <- Pokemon.random
    p2 <- Pokemon.random
    stream = Battle(p1, p2).unfold.map(_.toString).intersperse("\n")
  } yield Response(
    status = Status.OK,
    data = HttpData.fromStream(
      Stream(s"$p1 vs $p2\n") ++ stream
    )
  )
}
