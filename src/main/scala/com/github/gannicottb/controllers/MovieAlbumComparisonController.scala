package com.github.gannicottb.controllers

import com.github.gannicottb.FMA.AlbumEntry
import com.github.gannicottb.{FMA, IMDB}
import com.github.gannicottb.IMDB.AkaEntry
import zhttp.http.{HttpData, Response, Status}
import zio.Task
import zio.stream.ZPipeline._

object MovieAlbumComparisonController {
  def show(search: String) = {
//    val combo1 = IMDB.streamRows.map(_.title) merge FMA.streamRows.map(_.albumTitle)
//    val combo2 = (IMDB.streamRows zip FMA.streamRows).collect {
//      case (movie, album) if movie.title.contains(search) || album.albumTitle.contains(search) =>
//    }
    val combo3 = IMDB.streamRows.merge(FMA.streamRows).collect {
      case AkaEntry(_, _, title, _, _, _, _, _) if title.contains(search) => s"Album: $title"
      case AlbumEntry(_, title, _) if title.contains(search)              => s"Movie: $title"
    }

    Response(
      status = Status.OK,
      data = HttpData.fromStream(
        combo3.take(1000).intersperse("\n")
      ) // Encoding content using a ZStream
    )
  }
}
