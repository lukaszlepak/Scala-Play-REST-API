package repositories

import java.sql.Timestamp

import javax.inject.Inject
import models._
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import scala.collection.immutable.SeqMap
import scala.concurrent.{ExecutionContext, Future}

class ProjectRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val projects = ProjectSchema.projects

  val tasks = TaskSchema.tasks

  val leftJoinProjectsTasks = projects joinLeft tasks on (_.id === _.project_id)

  def findProjectsWithTasks(idList: Option[Seq[Int]], beforeTS: Option[Timestamp], afterTS: Option[Timestamp], isDeleted: Option[Boolean], page:Option[Int], pageSize: Option[Int], sortBy: Option[String], order: Option[String]): Future[Map[Project, Seq[Task]]] = db.run {
    val filteredProjects = projects
      .filterOpt(idList)((row, idList) => row.id.inSet(idList))
      .filterOpt(beforeTS)(_.ts < _)
      .filterOpt(afterTS)(_.ts > _)
      .filterOpt(isDeleted) {
        case (row, true) => row.isDeleted.isEmpty
        case (row, false) => row.isDeleted.isDefined
      }

    val sortedProjects = (sortBy, order) match {
      case (Some("created_at"), Some("asc")) => filteredProjects.sortBy(_.ts.asc)
      case (Some("last_activity"), Some("asc")) => filteredProjects.sortBy(_.lastActivity.asc)
      case (Some("created_at"), Some("desc")) => filteredProjects.sortBy(_.ts.desc)
      case (Some("last_activity"), Some("desc")) => filteredProjects.sortBy(_.lastActivity.desc)
      case _ => filteredProjects
    }

    val pagedProjects = (page, pageSize) match {
      case (Some(p: Int), Some(ps: Int)) => sortedProjects.drop(p*ps).take(ps)
      case _ => sortedProjects
    }

    (pagedProjects joinLeft tasks on (_.id === _.project_id)).result.map {
      _.foldLeft(SeqMap.empty[Project, Seq[Task]]) {
        case (acc, (k, v)) => acc.updated(k, acc.getOrElse(k, Seq.empty[Task]) ++ v)
      }
    }
  }

  def findProjectWithTasks(id: Int): Future[Option[(Project, Seq[Option[Task]])]] = db.run {
    leftJoinProjectsTasks.filter(_._1.id === id).result
      .map { _.groupBy(_._1).map { case (k,v) => (k,v.map(_._2)) }.headOption }
  }

  def insertProject(name: String): Future[Int] = db.run {
    projects.filter(_.name === name).result.headOption.flatMap {
      case Some(_) => DBIO.successful(-1)
      case None => projects += Project(0, name, new Timestamp(System.currentTimeMillis()),  new Timestamp(System.currentTimeMillis()), None)
    }.transactionally
  }

  def updateProject(id: Int, name: String): Future[Int] = db.run {
    projects.filter(_.name === name).result.headOption.flatMap {
      case Some(_) => DBIO.successful(-1)
      case None => projects.filter(_.id === id).map(_.name).update(name)
    }.transactionally
  }

  def softDeleteProject(id: Int): Future[Int] = db.run {
    {
      val ts = new Timestamp(System.currentTimeMillis())
      projects.filter(_.id === id).result.headOption.flatMap {
        case Some(p) => tasks.filter(t => t.project_id === p.id).map(_.isDeleted).update(Some(ts)) andThen
          projects.filter(_.id === id).map(_.isDeleted).update(Some(ts))
        case None => DBIO.successful(0)
      }
    }.transactionally
  }
}
