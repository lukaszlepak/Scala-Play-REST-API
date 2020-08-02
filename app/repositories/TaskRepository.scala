package repositories

import java.sql.Timestamp

import javax.inject.Inject
import models.Task
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class TaskRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val tasks = TaskSchema.tasks

  def insertTask(project_id: Int, duration: Long, volume: Option[Int], description: Option[String]): Future[Int] = db.run {
    { val ts = new Timestamp(System.currentTimeMillis())
      tasks.filter(_.project_id === project_id).sortBy(_.ts.desc).result.headOption.flatMap {
      case Some(t) =>
        if ((t.ts.getTime + t.duration) < ts.getTime) {
          tasks += Task(0, project_id, ts, duration, volume, description, Timestamp.valueOf("0001-01-01 00:00:00"))
        } else DBIO.successful(0)
      case None =>
        tasks += Task(0, project_id, ts, duration, volume, description, Timestamp.valueOf("0001-01-01 00:00:00"))
    }}.transactionally
  }

  def updateTask(id: Int, project_id: Int, ts: Timestamp, duration: Long, volume: Option[Int], description: Option[String]): Future[Int] = db.run {
    {tasks.filter(_.project_id === project_id).sortBy(_.ts.desc).result.headOption.flatMap {
      case Some(t) =>
        if (t.id == id || ((t.ts.getTime + t.duration) < ts.getTime) ) {
          tasks.filter(_.id === id).map(_.isDeleted).update(new Timestamp(System.currentTimeMillis())) andThen
            (tasks += Task(0, project_id, ts, duration, volume, description, Timestamp.valueOf("0001-01-01 00:00:00")))
        } else DBIO.successful(0)
      case None =>
        DBIO.successful(-1)
    }}.transactionally
  }

  def softDeleteTask(id: Int): Future[Int] = db.run {
    tasks.filter(_.id === id).map(_.isDeleted).update(new Timestamp(System.currentTimeMillis()))
  }
}
