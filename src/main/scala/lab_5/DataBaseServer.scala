package lab_5

import com.bot4s.telegram.models.User
import com.bot4s.telegram.models.Message
import lab_2.GalleryBot
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey)
  def isBot = column[Boolean]("botFlag")
  def firstName = column[String]("firstName")
  def lastName = column[Option[String]]("lastName", O.Default(None))
  def username = column[Option[String]]("username", O.Default(None))
  def languageCode = column[Option[String]]("languageCode", O.Default(None))

  def * = (id, isBot, firstName, lastName, username, languageCode) <> (User.tupled, User.unapply)
}

case class MyMessage(messageId: Option[Int], fromId: Int, toId: Int, text: String)
class Messages(tag: Tag) extends Table[MyMessage](tag, "messages") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def fromId = column[Int]("from")
  def toId = column[Int]("to")
  def text = column[String]("text")

  def * = (id.?, fromId, toId, text) <> (MyMessage.tupled, MyMessage.unapply)
}

case class CatQuerry(querryId: Option[Int], catURL: String, userId: Int)
class CatQuerries(tag: Tag) extends Table[CatQuerry](tag, "querries") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def catURL = column[String]("catURL")
  def userId = column[Int]("userId")

  def * = (id.?, catURL, userId) <> (CatQuerry.tupled, CatQuerry.unapply)
}

class DataBaseServer (val db: Database)(implicit val ec: ExecutionContext, implicit val galleryBot: GalleryBot) {
  private val usersBase = TableQuery[Users]
  private val messagesBase = TableQuery[Messages]
  private val querriesBase = TableQuery[CatQuerries]


  def init: Future[Unit] = {
    val transaction = for {
      _ <- usersBase.schema.createIfNotExists
      _ <- messagesBase.schema.createIfNotExists
      _ <- querriesBase.schema.createIfNotExists
    } yield ()
    db.run(transaction)
  }

  def addUser(implicit msg: Message): Future[Unit] = {
    val setup = DBIO.seq(
      usersBase += msg.from.get)
    db.run(setup)

  }

  def users: Future[String] =  {
    db.run(usersBase.result).map(_.mkString)
  }

  def addMessage(user: User, words: Array[String]): Future[Unit] = {
    val setup = DBIO.seq(
      messagesBase += MyMessage(None, user.id, words(1).toInt, words.drop(2).fold("") { (z, i) => z ++ " " ++ i}))
    db.run(setup)
  }

  def getMessages(id : Int): Future[List[String]] = {
    val transaction = for {
      res <- messagesBase.filter(_.toId === id).map(_.text).result
      _ <- messagesBase.filter(_.toId === id).delete
    } yield res.toList
    db.run(transaction)
  }

  def getImage(querry: String, user: User): Future[String] = galleryBot.getLink(querry).flatMap { catURL =>
    val setup = DBIO.seq(
      querriesBase += CatQuerry(None, catURL, user.id))
    db.run(setup)
    Future.successful(catURL)
  }

  def getStats(nameOrId: String): Future[Option[String]] = {
    val optId = Try(nameOrId.toInt).toOption
    val transaction = for {
      id <- usersBase.filter{ user => user.id === optId || user.firstName === nameOrId }.map(_.id).result.headOption
      stats <- querriesBase.filter(_.userId.? === id).map(_.catURL).result
    } yield id.map(_ => stats.toList)
    db.run(transaction).map(_.map(links => links.mkString("\n")))
  }

}
