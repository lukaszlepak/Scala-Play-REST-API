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

    def * = (id, name, ts) <> ((Project.apply _).tupled, Project.unapply)
  }

  private val projects = TableQuery[ProjectsTable]

  def selectAll: Future[Seq[Project]] = db.run { projects.result }

  def insert(name: String, ts: Timestamp): Future[Int] = db.run {
    projects.filter(_.name === name).result.headOption.flatMap {
      case Some(p) => DBIO.successful(p.id)
      case None => (projects returning projects.map(_.id)) += Project(0, name, ts)
    }.transactionally
  }
}
