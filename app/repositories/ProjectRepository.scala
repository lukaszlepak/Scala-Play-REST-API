package repositories

import java.sql.Timestamp

import javax.inject.Inject
import models._
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ProjectRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val projects = ProjectSchema.projects

  val tasks = TaskSchema.tasks

  val leftJoinProjectsTasks = projects joinLeft tasks on ( (p: ProjectSchema.ProjectTable, t: TaskSchema.TaskTable) => p.id === t.project_id)

  def findProjectsWithTasks(): Future[Map[Project, Seq[Option[Task]]]] = db.run {
    leftJoinProjectsTasks.result
      .map { _.groupBy(_._1).map { case (k,v) => (k,v.map(_._2)) } }
  }

  def findProjectWithTasks(name: String): Future[Option[(Project, Seq[Option[Task]])]] = db.run {
    leftJoinProjectsTasks.filter(_._1.name === name).result
      .map { _.groupBy(_._1).map { case (k,v) => (k,v.map(_._2)) }.headOption }
  }

  def insertProject(name: String): Future[Int] = db.run {
    projects.filter(_.name === name).result.headOption.flatMap {
      case Some(_) => DBIO.successful(-1)
      case None => projects += Project(0, name, new Timestamp(System.currentTimeMillis()), Timestamp.valueOf("0001-01-01 00:00:00"))
    }.transactionally
  }

  def updateProject(oldName: String, newName: String): Future[Int] = db.run {
    projects.filter(_.name === newName).result.headOption.flatMap {
      case Some(_) => DBIO.successful(-1)
      case None => projects.filter(_.name === oldName).map(_.name).update(newName)
    }.transactionally
  }

  def softDeleteProject(name: String): Future[Int] = db.run {
    {
      val ts = new Timestamp(System.currentTimeMillis())
      projects.filter(_.name === name).result.headOption.flatMap {
        case Some(p) => tasks.filter(t => t.project_id === p.id).map(_.isDeleted).update(ts)
        case None => DBIO.successful(-1)
      } andThen
        projects.filter(_.name === name).map(_.isDeleted).update(ts)
    }.transactionally
  }
}
