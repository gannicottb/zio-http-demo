package com.github.gannicottb

import kantan.csv._
import kantan.csv.generic._
import kantan.csv.ops._
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.stream.ZPipeline.{splitLines, utf8Decode}
import zio.stream.{Stream, ZStream}

object FMA extends DataSource {
  override def local: ZStream[Any, Throwable, Byte] =
    Stream.fromFileString("/home/brandon/Downloads/fma_metadata/raw_albums.csv")

  override def remote: ZStream[EventLoopGroup with ChannelFactory, Throwable, Byte] = ???

  // Because the source data contains unescaped newline characters, I have to skip rows that contain them
  def streamRows = local
    .via(utf8Decode >>> splitLines) // this csv has internal newline characters which goofs up any attempt to read it
    .drop(1)
    .map(row => AlbumEntry.fromRow(row).toOption)
    .collectSome

  case class AlbumEntry(
      albumId: String,
      albumTitle: String,
      artistName: String
  )
  object AlbumEntry {
    implicit val albumEntryDecoder = RowDecoder.decoder(0, 12, 16)(AlbumEntry.apply)
    val empty = AlbumEntry("", "", "")
    def fromRow(row: String) = row.readCsvRow[AlbumEntry](rfc.quoteAll.withQuote('"'))
    def fromRows(rawCsv: String) = rawCsv.asCsvReader[AlbumEntry](rfc.withHeader.quoteAll)
  }

}
