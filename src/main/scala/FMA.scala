import kantan.csv.{RowDecoder, rfc}
import kantan.csv.ops.{toCsvInputOps, toCsvRowReadingOps}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.stream.{Stream, ZStream}

object FMA extends DataSource {
  override def local: ZStream[Any, Throwable, Byte] =
    Stream.fromFileString("/home/brandon/Downloads/fma_metadata/raw_albums.csv")

  override def remote: ZStream[EventLoopGroup with ChannelFactory, Throwable, Byte] = ???

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
