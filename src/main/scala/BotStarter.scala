package bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.{FutureSttpClient, ScalajHttpClient}
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{ChatId, User}
import com.softwaremill.sttp.{SttpBackend, SttpBackendOptions}
import com.softwaremill.sttp.okhttp.{OkHttpBackend, OkHttpFutureBackend}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

class BotStarter(override val client: RequestHandler[Future]) extends TelegramBot
  with Polling
  with Commands[Future] {

  var users: List[User] = List()
  onCommand("/start") { implicit msg =>
    val user = msg.from match {
      case Some(x) => x
    }
    users = user :: users
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
    val text = msg.text match {
      case Some(x) => x
    }
    val words = text.split(" ")
    if (words.size < 2) {
      reply("Sdohni Tvar").void
    } else {
      request(SendMessage(ChatId(words(1)), words.drop(2).fold("") { (z, i) => z ++ " " ++ i})).void
    }
  }

}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val tokenFile = Source.fromFile("src/main/scala/token.txt")
    val token = tokenFile.mkString
    tokenFile.close()

    val bot = new BotStarter(new FutureSttpClient(token))
    Await.result(bot.run(), Duration.Inf)
  }
}