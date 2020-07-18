package models

import java.sql.Timestamp

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile

class ProjectRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private class ProjectsTable(tag: Tag) extends Table[Project](tag, "projects") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def ts = column[Timestamp]("ts")

    def isDeleted = column[Timestamp]("isdeleted")

    def * = (id, name, ts, isDeleted) <> ((Project.apply _).tupled, Project.unapply)
  }

  private val projects = TableQuery[ProjectsTable]

  def selectAll: Future[Seq[Project]] = db.run { projects.result }

  def select(name: String): Future[Option[Project]] = db.run {
    projects.filter(_.name === name).result.headOption
  }

  def insert(name: String, ts: Timestamp): Future[Int] = db.run {
    projects.filter(_.name === name).result.headOption.flatMap {
      case Some(p) => DBIO.successful(p.id)
      case None => (projects returning projects.map(_.id)) += Project(0, name, ts, Timestamp.valueOf("0001-01-01 00:00:00"))
    }.transactionally
  }

  def update(oldName: String, newName: String): Future[Int] = db.run {
    projects.filter(_.name === newName).result.headOption.flatMap {
      case Some(p) => DBIO.successful(p.id)
      case None => projects.filter(_.name === oldName).map(_.name).update(newName)
    }.transactionally
  }

  def softDelete(name: String, ts: Timestamp): Future[Int] = db.run {
    projects.filter(p => p.name === name && p.isDeleted === Timestamp.valueOf("0001-01-01 00:00:00")).map(_.isDeleted).update(ts)
  }
}
