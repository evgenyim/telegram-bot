package lab_4


import com.softwaremill.sttp.{Response, SttpBackend}
import lab_2.{Data, GalleryBot, ImageResponse, Images, Rand}
import org.json4s.native.Serialization
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object TestRandomizer extends Rand() {
  override def randElem[T](l: List[T]): T = l.head
}

class GalleryBotTest extends AnyFlatSpec with Matchers with MockFactory {
  trait mocks {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val sttpBackend: SttpBackend[Future, Nothing] = mock[SttpBackend[Future, Nothing]]
    implicit val serialization: Serialization.type = org.json4s.native.Serialization


    val service = new GalleryBot(TestRandomizer)
  }


  "ServiceRest" should "return cat link" in new mocks {
    (sttpBackend.send[ImageResponse] _).expects(*).returning(Future.successful(
      Response.ok(ImageResponse(List(Data(List(Images("https://i.imgur.com/IzXea4I.jpg"))),
                                     Data(List(Images("https://i.imgur.com/1oxbwfr.jpg"),
                                               Images("https://i.imgur.com/2U2LPuw.jpg"),
                                               Images("https://i.imgur.com/sYFPuR8.jpg"))))))
    ))

    val result: String = Await.result(service.getLink("cats"), Duration.Inf)

    result shouldBe """https://i.imgur.com/IzXea4I.jpg"""
  }
}