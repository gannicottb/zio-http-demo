import controllers.{MatchupController, TypeController}
import model.{Matchup, Type}
import zhttp.http._
import zhttp.service.Server
import zio.{UIO, _}

import java.io.IOException

/** Pokemon Type API
  */
object Main extends ZIOAppDefault {

  val api: HttpApp[Console, IOException] = Http.collectZIO[Request] {
    case req @ Method.GET -> !! / "matchup" => MatchupController.show(req)
    case Method.GET -> "type" /: id =>
      UIO(id.toString().drop(1)) // zio-http does not have good support for request matching, sadly.
        .flatMap(TypeController.show)
  }

  // Run it like any simple app
  override val run: ZIO[ZEnv with ZIOAppArgs, Throwable, Nothing] =
    Server.start(8090, api)
}
