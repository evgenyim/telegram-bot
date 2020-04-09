package lab_2

import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.sttp.{SttpBackend, SttpBackendOptions}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s._
import org.json4s.native.Serialization

case class ImageResponse(data: List[Data])
case class Data(images: List[Images])
case class Images(link: String)

class GalleryBot(val randomizer: Rand = Randomizer)(implicit
                 backend: SttpBackend[Future, Nothing],
                 ec: ExecutionContext,
                 serialization: Serialization.type) {
  def getLink(searchQuery: String): Future[String] = {
    val request = sttp
      .header("Authorization", "Client-ID e99b774b2ac6582")
      .get(uri"https://api.imgur.com/3/gallery/search?q=$searchQuery")
      .response(asJson[ImageResponse])

    backend.send(request).map { response =>
      randomizer.randElem(response.unsafeBody.data.flatMap(_.images)).link
    }
  }

}
