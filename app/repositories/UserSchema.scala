package repositories

import java.util.UUID

import repositories.ProjectSchema.api

object UserSchema {

  import api._

  class UserTable(tag: Tag) extends Table[UUID](tag, "users") {

    def uuid = column[UUID]("uuid", O.PrimaryKey, O.AutoInc)

    def * = uuid
  }

  val users = TableQuery[UserTable]
}
