package lab_2

import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.sttp.{SttpBackend, SttpBackendOptions}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s._

case class Response(data: List[Data])
case class Data(images: List[Images])
case class Images(link: String)

class GalleryBot(implicit
                 backend: SttpBackend[Future, Nothing],
                 ec: ExecutionContext) {

  implicit val serialization =  org.json4s.native.Serialization

  def getLink(searchQuery: String): String = {
    val request = sttp
      .header("Authorization", "Client-ID e99b774b2ac6582")
      .get(uri"https://api.imgur.com/3/gallery/search?q=$searchQuery")
      .response(asJson[Response])

    val res = backend.send(request).map { response =>
      val len = response.unsafeBody.data.size
      response.unsafeBody
        .data(scala.util.Random.nextInt(len)).images.head.link
    }
    Await.ready(res, Duration.Inf)
    res.value match {
      case Some(x) => x.get
      case _ => "ERROR"
    }
  }

}
