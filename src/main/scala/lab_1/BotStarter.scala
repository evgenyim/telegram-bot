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
import lab_5.DataBaseServer
import org.json4s.native.Serialization

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import slick.jdbc.H2Profile.api._

class BotStarter(override val client: RequestHandler[Future], galleryBot: GalleryBot, val dbs: DataBaseServer) extends TelegramBot

  with Polling
  with Commands[Future] {

  val users: mutable.MutableList[User] = mutable.MutableList[User]()
  var messages: List[(String, Int, String)] = List[(String, Int, String)]()
  onCommand("/start") { implicit msg =>
    dbs.addUser
    reply(s"Hi!").void
  }

  onCommand("/users") { implicit msg =>
    dbs.users.flatMap(reply(_)).void
  }

  onCommand("/send") { implicit msg =>
    val text = msg.text.get
    val words = text.split(" ")
    if (words.size < 2) {
      reply("Sdohni Tvar").void
    } else {
      val user = msg.from.get
      dbs.addMessage(msg.from.get, words)
      reply("Ok").void
    }
  }

  onCommand("/check") { implicit msg =>
    val id = msg.from.get.id
    dbs.getMessages(id).flatMap {
      messages =>
        if (messages.isEmpty) {
          reply("There are no new messages").void
        } else {
          messages.foreach(reply(_).void)
          Future.unit
        }
    }
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
    val db = Database.forConfig("h2mem1")
    try {
      val tokenFile = Source.fromFile("src/main/scala/token.txt")
      val token = tokenFile.mkString
      tokenFile.close()

      implicit val galleryBot: GalleryBot = new GalleryBot
      val bot = new BotStarter(new FutureSttpClient(token), galleryBot, new DataBaseServer(db))
      Await.result(bot.dbs.init, Duration.Inf)
      Await.result(bot.run(), Duration.Inf)
    } finally db.close
  }
}