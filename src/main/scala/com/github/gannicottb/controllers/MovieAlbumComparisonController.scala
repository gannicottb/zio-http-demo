package com.github.gannicottb.controllers

import com.github.gannicottb.FMA.AlbumEntry
import com.github.gannicottb.{FMA, IMDB}
import com.github.gannicottb.IMDB.AkaEntry
import zhttp.http.{HttpData, Response, Status}
import zio.Task
import zio.IO
import zio.stream.ZPipeline._
import zio.stream._

object MovieAlbumComparisonController {
  def show(search: String) = {
    val mergedStream = IMDB.streamRows
      // .merge(FMA.streamRows) // this data won't unzip on MacOS -_-
      .mapZIOPar(128) {
        case AkaEntry(_, _, title, _, _, _, _, _) =>
          IO(findInString(search, title).map(found => s"Movie: $found"))
        // case AlbumEntry(_, title, _) =>
        //   IO(findInString(search, title).map(found => s"Album: $found"))
        case _ => IO(None)
      }
      .collectSome

    Response(
      status = Status.OK,
      data = HttpData.fromStream(
        mergedStream.take(1000).intersperse("\n")
      ) // Encoding content using a ZStream
    )
  }

  private def findInString(search: String, base: String) = {
    val replaced = base.replace(search, s"[$search]")
    if (replaced != base) Some(replaced) else None
  }

}
