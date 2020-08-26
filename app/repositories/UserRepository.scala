package repositories

import java.util.UUID

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val users = UserSchema.users

  def findUser(uuid: UUID): Future[Boolean] = db.run{
    users.filter(_.uuid === uuid).exists.result
  }
}
