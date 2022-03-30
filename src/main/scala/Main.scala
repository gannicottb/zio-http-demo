import model.{Matchup, Type}
import zhttp.http._
import zhttp.service.Server
import zio._

/** Pokemon Type API
  */
object Main extends ZIOAppDefault {

  val api: HttpApp[Any, Nothing] = Http.collect[Request] { case req @ Method.GET -> !! / "matchup" =>
    def parseTypeFromParam(key: String): Option[Type] = for {
      // Get all of the params for the two keys we care about
      params <- req.url.queryParams.get(key)
      // Just take the first value
      firstParam <- params.headOption
      // Then look up the value to get the Type
      theType <- Type.ofNameInsensitive(firstParam)
    } yield theType

    (for {
      attackerType <- parseTypeFromParam("attacker")
      defenderType <- parseTypeFromParam("defender")
    } yield {
      val multiplier = Matchup.compute(attackerType, defenderType)
      Response.text(s"${attackerType.name} is ${multiplier}x against ${defenderType.name}")
    }).getOrElse(Response.status(Status.BAD_REQUEST))
  }

  // Run it like any simple app
  override val run: ZIO[ZEnv with ZIOAppArgs, Throwable, Nothing] =
    Server.start(8090, api)
}
