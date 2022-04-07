package com.github.gannicottb

import com.github.gannicottb.controllers._
import zhttp.http._
import zhttp.service.Server
import zio._

import java.io.IOException

/** Pokemon Type API
  */
object Main extends ZIOAppDefault {

  val pokemonApi: HttpApp[Console with Random with Clock, IOException] = Http.collectZIO[Request] {
    case Method.GET -> !! / "matchup" / a / "vs" / d => MatchupController.show(a, d)
    case Method.GET -> !! / "type" / id              => TypeController.show(id)
    case Method.GET -> !! / "pokemon" / "random"     => PokemonController.random
  }

  val streamingApi = Http.collect[Request] { case Method.GET -> !! / "stream" / search =>
    MovieAlbumComparisonController.show(search)
  }

  // Run it like any simple app
  override val run: ZIO[ZEnv with ZIOAppArgs, Throwable, Nothing] =
    Server.start(8090, pokemonApi ++ streamingApi)
}
