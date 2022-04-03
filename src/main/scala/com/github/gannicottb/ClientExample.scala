package com.github.gannicottb

import com.github.gannicottb.FMA.AlbumEntry
import com.github.gannicottb.IMDB.AkaEntry
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.stream.ZPipeline.{gunzip, splitLines, utf8Decode}
import zio.{Console, Task, ZIOAppDefault}

object ClientExample extends ZIOAppDefault {
  val env = ChannelFactory.auto ++ EventLoopGroup.auto()

  // Test streaming and parsing IMDB rows
  val printOutSomeIMDBRows = for {
    _ <- IMDB.local
      .via(gunzip() >>> utf8Decode >>> splitLines >>> IMDB.removeInvalidEscapeCharacters)
      .drop(1) // drop the header
      .take(20)
      .mapZIO(row => Task.fromEither(AkaEntry.fromRow(row)))
      .tap(s => Console.printLine(s))
      .runDrain
      .tapError { case e =>
        Console.printLine(s"${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
      }
  } yield ()

  val printOutSomeAlbumRows = for {
    _ <- FMA.local
      .via(utf8Decode >>> splitLines) // this csv has internal newline characters which goofs up any attempt to read it
      .drop(1)
      .take(100)
      .map(row => AlbumEntry.fromRow(row).toOption)
      .filter(_.isDefined)
      .tap(Console.printLine(_)) // debug
      .runDrain
      .tapError(e => Console.printLine(s"${e.getMessage}\n${e.getStackTrace.mkString("\n")}"))
  } yield ()

  val program = printOutSomeAlbumRows

  override val run =
    program.exitCode.provideCustomLayer(env)

}
