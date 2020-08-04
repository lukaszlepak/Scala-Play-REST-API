package repositories

import models.Project
import java.sql.Timestamp

import slick.jdbc.JdbcProfile

object ProjectSchema extends JdbcProfile {

  import api._

  class ProjectTable(tag: Tag) extends Table[Project](tag, "projects") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def ts = column[Timestamp]("ts")

    def lastActivity = column[Timestamp]("lastactivity")

    def isDeleted = column[Option[Timestamp]]("isdeleted")

    def * = (id, name, ts, lastActivity, isDeleted) <> ((Project.apply _).tupled, Project.unapply)
  }

  val projects = TableQuery[ProjectTable]
}
