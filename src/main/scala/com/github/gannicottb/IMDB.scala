package com.github.gannicottb

import kantan.codecs.Codec
import kantan.csv._
import kantan.csv.generic._
import kantan.csv.ops.toCsvRowReadingOps
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.Task
import zio.stream.ZPipeline._
import zio.stream.{Stream, ZPipeline, ZStream}

object IMDB extends DataSource {
  // Downloaded from https://datasets.imdbws.com/
  def local = Stream.fromFileString("/home/brandon/Downloads/title.akas.tsv.gz")

  // This doesn't quite work, not sure why yet
  def remote: ZStream[EventLoopGroup with ChannelFactory, Throwable, Byte] = for {
    body <- Stream.fromZIO(Client.request("https://datasets.imdbws.com/title.akas.tsv.gz").flatMap(_.body))
    byte <- Stream.fromChunk(body)
  } yield byte

  def streamRows = local
    .via(gunzip() >>> utf8Decode >>> splitLines >>> IMDB.removeInvalidEscapeCharacters)
    .drop(1) // drop the header
    .map(row => AkaEntry.fromRow(row).toOption)
    .collectSome

  val removeInvalidEscapeCharacters: ZPipeline[Any, Nothing, String, String] =
    ZPipeline.map[String, String](_.replaceAll("\\\\[^btnfr\"]", ""))

  // Machinery to parse data
  case class AkaEntry(
      titleId: String,
      ordering: Option[Int],
      title: String,
      region: String,
      language: String,
      types: Seq[String],
      attributes: Seq[String],
      isOriginalTitle: Option[Int]
  )
  object AkaEntry {
    implicit val SeqCodec: Codec[String, Seq[String], DecodeError, codecs.type] =
      CellCodec.from(s =>
        // IMDB "enumerated field" columns are space delimited
        DecodeResult(s.split(" ").toSeq)
      )(d => d.mkString(" "))

    def empty = AkaEntry("", None, "", "", "", Seq[String](), Seq[String](), None)
    def fromRow(row: String) = row.readCsvRow[AkaEntry](rfc.withCellSeparator('\t'))
  }

}
