package repositories

import models.Task
import java.sql.Timestamp

import slick.jdbc.JdbcProfile

object TaskSchema extends JdbcProfile {

  import api._

  class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def project_id = column[Int]("project_id")

    def ts = column[Timestamp]("ts")

    def duration = column[Long]("duration")

    def volume = column[Option[Int]]("volume")

    def description = column[Option[String]]("description")

    def isDeleted = column[Timestamp]("isdeleted")

    def * = (id, project_id, ts, duration, volume, description, isDeleted) <> ((Task.apply _).tupled, Task.unapply)
  }

  val tasks = TableQuery[TaskTable]
}
