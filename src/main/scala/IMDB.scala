import kantan.codecs.Codec
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.stream.{Stream, ZChannel, ZPipeline, ZStream}
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import zio.stream.ZPipeline._

object IMDB extends DataSource {
  // Downloaded from https://datasets.imdbws.com/
  def local = Stream.fromFileString("/home/brandon/Downloads/title.akas.tsv.gz")

  // This doesn't quite work, not sure why yet
  def remote: ZStream[EventLoopGroup with ChannelFactory, Throwable, Byte] = for {
    body <- Stream.fromZIO(Client.request("https://datasets.imdbws.com/title.akas.tsv.gz").flatMap(_.body))
    byte <- Stream.fromChunk(body)
  } yield byte

  val removeInvalidEscapeCharacters: ZPipeline[Any, Nothing, String, String] =
    ZPipeline.map[String, String](_.replaceAll("\\\\[^btnfr\"]", ""))

  // Machinery to parse data
  case class AkaEntry(
      titleId: String,
      ordering: Int,
      title: String,
      region: String,
      language: String,
      types: Seq[String],
      attributes: Seq[String],
      isOriginalTitle: Int
  )
  object AkaEntry {
    implicit val SeqCodec: Codec[String, Seq[String], DecodeError, codecs.type] =
      CellCodec.from(s =>
        // IMDB "enumerated field" columns are space delimited
        DecodeResult(s.split(" ").toSeq)
      )(d => d.mkString(" "))

    def empty = AkaEntry("", 1, "", "", "", Seq[String](), Seq[String](), 1)
    def fromRow(row: String) = row.readCsvRow[AkaEntry](rfc.withCellSeparator('\t'))
  }

}
