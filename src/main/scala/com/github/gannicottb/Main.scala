package com.github.gannicottb

import com.github.gannicottb.controllers.{MatchupController, MovieAlbumComparisonController, TypeController}
import zhttp.http._
import zhttp.service.Server
import zio.{Console, ZEnv, ZIO, ZIOAppArgs, ZIOAppDefault}

import java.io.IOException

/** Pokemon Type API
  */
object Main extends ZIOAppDefault {

  val pokemonApi: HttpApp[Console, IOException] = Http.collectZIO[Request] {
    case Method.GET -> !! / "matchup" / a / "vs" / d => MatchupController.show(a, d)
    case Method.GET -> !! / "type" / id              => TypeController.show(id)
  }

  val streamingApi = Http.collect[Request] { case Method.GET -> !! / "stream" / search =>
    MovieAlbumComparisonController.show(search)
  }

  // Run it like any simple app
  override val run: ZIO[ZEnv with ZIOAppArgs, Throwable, Nothing] =
    Server.start(8090, pokemonApi ++ streamingApi)
}
