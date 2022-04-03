import IMDB.AkaEntry
import controllers.{MatchupController, TypeController}
import model.{Matchup, Type}
import zhttp.http._
import zhttp.service.Server
import zio.stream.ZPipeline._
import zio.{UIO, _}

import java.io.IOException

/** Pokemon Type API
  */
object Main extends ZIOAppDefault {

  val pokemonApi: HttpApp[Console, IOException] = Http.collectZIO[Request] {
    case req @ Method.GET -> !! / "matchup" => MatchupController.show(req)
    case Method.GET -> !! / "type" / id     => TypeController.show(id)
  }

  val streamingApi = Http.collect[Request] { case Method.GET -> !! / "stream" =>
    Response(
      status = Status.OK,
      data = HttpData.fromStream(
        IMDB.local
          .via(gunzip() >>> utf8Decode >>> splitLines >>> IMDB.removeInvalidEscapeCharacters)
          .drop(1) // drop the header
          .take(1000)
          .mapZIO(row => Task.fromEither(AkaEntry.fromRow(row)))
          .map(_.toString)
      ) // Encoding content using a ZStream
    )
  }

  // Run it like any simple app
  override val run: ZIO[ZEnv with ZIOAppArgs, Throwable, Nothing] =
    Server.start(8090, pokemonApi ++ streamingApi)
}
