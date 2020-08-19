package repositories

import java.sql.Timestamp

import javax.inject.Inject
import models.Task
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class TaskRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val tasks = TaskSchema.tasks

  val projects = ProjectSchema.projects

  def latestTask(project_id: Int) =
    tasks
      .filter(task => task.project_id === project_id && task.isDeleted.isEmpty)
      .sortBy(_.ts.desc)
      .take(1)

  def insertTask(project_id: Int, duration: Long, volume: Option[Int], description: Option[String]): Future[Try[Int]] = db.run {
    {
      val ts = new Timestamp(System.currentTimeMillis())
      projects
        .filter(p => p.id === project_id && p.isDeleted.isEmpty)
        .result.headOption.flatMap {
          case Some(_) => latestTask(project_id)
            .result.headOption.flatMap {
            case Some(t) if (t.ts.getTime + t.duration) > ts.getTime =>
              DBIO.failed(new IllegalArgumentException("CANNOT INSERT TASK, LAST TASK IS NOT FINISHED YET"))
            case _ =>
              projects.filter(_.id === project_id).map(_.lastActivity).update(ts) andThen
                (tasks += Task(0, project_id, ts, duration, volume, description, None))
          }
          case _ =>
            DBIO.successful(0)
      }.transactionally.asTry
    }
  }

  def updateTask(id: Int, project_id: Int, ts: Timestamp, duration: Long, volume: Option[Int], description: Option[String]): Future[Try[Int]] = db.run {
    { val ts = new Timestamp(System.currentTimeMillis())
      projects
        .filter(p => p.id === project_id && p.isDeleted.isEmpty)
        .result.headOption.flatMap {
          case Some(_) => latestTask(project_id)
            .result.headOption.flatMap {
            case Some(t) if (t.ts.getTime + t.duration) >= ts.getTime && (t.id != id) =>
              DBIO.failed(new IllegalArgumentException("CANNOT UPDATE TASK, LAST TASK IS NOT FINISHED YET"))
            case Some(_) =>
              tasks
                .filter(task => task.id === id && task.isDeleted.isEmpty)
                .map(_.isDeleted)
                .update(Some(ts)).flatMap {
                case x if x != 0 => (tasks += Task(0, project_id, ts, duration, volume, description, None)) andThen
                  projects
                    .filter(_.id === project_id)
                    .map(_.lastActivity)
                    .update(ts)
                case _ => DBIO.successful(0)
              }
            case None => DBIO.successful(0)
            }
          case None => DBIO.successful(0)
        }
    }.transactionally.asTry
  }

  def softDeleteTask(id: Int): Future[Int] = db.run {
    {tasks
      .filter(t => t.id === id && t.isDeleted.isEmpty )
      .map(_.project_id)
      .result.headOption.flatMap {
        case Some(pid) => {
          tasks
            .filter(task => task.project_id === pid && task.isDeleted.isEmpty && task.id =!= id)
            .map(_.ts)
            .sortBy(_.desc)
            .take(1)
            .result.headOption.flatMap {
              case Some(ts) => {
                projects
                  .filter(_.id === pid)
                  .map(_.lastActivity)
                  .update(ts) andThen
                    tasks
                      .filter(task => task.id === id && task.isDeleted.isEmpty)
                      .map(_.isDeleted)
                      .update(Some(new Timestamp(System.currentTimeMillis())))
              }
              case None => {
                projects
                  .filter(_.id === pid)
                  .map(_.ts)
                  .result.headOption.flatMap {
                    case Some(ts) => {
                      projects
                        .filter(_.id === pid)
                        .map(_.lastActivity)
                        .update(ts) andThen
                        tasks
                          .filter(task => task.id === id && task.isDeleted.isEmpty)
                          .map(_.isDeleted)
                          .update(Some(new Timestamp(System.currentTimeMillis())))
                    }
                  }
              }
            }
        }
        case None => DBIO.successful(0)
      }
    }.transactionally
  }
}
