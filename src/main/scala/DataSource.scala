import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.stream.ZStream

trait DataSource {
  def local: ZStream[Any, Throwable, Byte]
  def remote: ZStream[EventLoopGroup with ChannelFactory, Throwable, Byte]
}
