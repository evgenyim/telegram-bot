package lab_5

import com.bot4s.telegram.models.User
import com.bot4s.telegram.models.Message
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

//case class User(id: Int, firstName: String, lastName: Option[String], username: Option[String])

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey)
  def isBot = column[Boolean]("botFlag")
  def firstName = column[String]("firstName")
  def lastName = column[Option[String]]("lastName", O.Default(None))
  def username = column[Option[String]]("username", O.Default(None))
  def languageCode = column[Option[String]]("languageCode", O.Default(None))

  def * = (id, isBot, firstName, lastName, username, languageCode) <> (User.tupled, User.unapply)

}

class DataBaseServer (val db: Database)(implicit val ec: ExecutionContext) {
  private val usersBase = TableQuery[Users]

  def addUser(implicit msg: Message): Future[Unit] = {
    msg.from match {
      case Some(user) => db.run(usersBase.insertOrUpdate(user)).map(_ => ())
      case None => Future.unit
    }
  }
}
