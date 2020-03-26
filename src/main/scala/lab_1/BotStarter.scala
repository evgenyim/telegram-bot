package lab_1
import lab_2.GalleryBot
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{ChatId, User}
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.sttp.{SttpBackend, SttpBackendOptions}
import org.json4s.native.Serialization

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

class BotStarter(override val client: RequestHandler[Future], galleryBot: GalleryBot) extends TelegramBot

  with Polling
  with Commands[Future] {

  val users: mutable.MutableList[User] = mutable.MutableList[User]()
  var messages: List[(String, Int, String)] = List[(String, Int, String)]()
  onCommand("/start") { implicit msg =>
    val user = msg.from.get
    users += user
    reply(s"Hi!").void
  }

  onCommand("/users") { implicit msg =>
    var text = ""
    if (users.isEmpty) {
      reply("users is empty")
    }
    for (user <- users ) {
      text += s"name:  ${user.firstName}, ID: ${user.id.toString}\n"
    }
    reply(text).void
  }

  onCommand("/send") { implicit msg =>
    val text = msg.text.get
    val words = text.split(" ")
    if (words.size < 2) {
      reply("Sdohni Tvar").void
    } else {
      val user = msg.from.get
      messages = (user.firstName, words(1).toInt, words.drop(2).fold("") { (z, i) => z ++ " " ++ i}) :: messages
      reply("Ok").void
    }
  }

  onCommand("/check") {implicit msg =>
    val user = msg.from.get
    reply(messages.filter(_._2 == user.id.toInt).map(x => s"from ${x._1}: ${x._3}").mkString("\n")).void
  }

  onCommand("/image") { implicit msg =>
    val text = msg.text.get
    val res = galleryBot.getLink(text)
    res.flatMap(link => reply(link)).void
  }

}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )
    implicit val serialization: Serialization.type = org.json4s.native.Serialization

    val tokenFile = Source.fromFile("src/main/scala/token.txt")
    val token = tokenFile.mkString
    tokenFile.close()

    implicit val galleryBot: GalleryBot = new GalleryBot
    val bot = new BotStarter(new FutureSttpClient(token), galleryBot)
    Await.result(bot.run(), Duration.Inf)
  }
}