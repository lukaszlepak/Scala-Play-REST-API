package controllers

import java.sql.Timestamp

import scala.util.{Failure, Success, Try}
import javax.inject.Inject
import jsonErrors.JSONError
import models.ProjectWithTasks
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import repositories.ProjectRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProjectController @Inject()(repository: ProjectRepository, cc: ControllerComponents) extends AbstractController(cc) with JSONError {

  def findProjectsWithTasks(idList: Seq[Int], beforeTS: Option[String], afterTS: Option[String], isDeleted: Option[Boolean],
                            page: Option[Int], pageSize: Option[Int], sortBy: Option[String], order: Option[String]): Action[AnyContent] = Action.async { implicit request =>

    val params = for {
      bts <- Try(beforeTS.map(Timestamp.valueOf))
      ats <- Try(afterTS.map(Timestamp.valueOf))
      f <- Try(require(order.isEmpty || order.contains("asc") || order.contains("desc")))
      l <- Try(require(sortBy.isEmpty || sortBy.contains("last_activity") || sortBy.contains("created_at")))
    } yield (bts, ats)

    params match {
      case Success(times) => {
        val foundProjects = repository.findProjectsWithTasks(
          if(idList.isEmpty) None else Option(idList),
          times._1,
          times._2,
          isDeleted,
          page,
          pageSize,
          sortBy,
          order
        )

        for {
          p <- foundProjects
        } yield Ok(Json.toJson(p.map(t => ProjectWithTasks(t._1.id, t._1.name, t._1.ts, t._1.lastActivity, t._1.isDeleted, t._2))))
      }
      case Failure(e) => Future.successful(BadRequestJson(e.getMessage))
    }
  }
    
  def findProjectWithTasks(id: Int): Action[AnyContent] = Action.async { implicit request =>
    val futureProject = repository.findProjectWithTasks(id)

    futureProject.map {
      case Some(t) => Ok(Json.toJson(ProjectWithTasks(t._1.id, t._1.name, t._1.ts, t._1.lastActivity, t._1.isDeleted, t._2.flatten)))
      case None => NotFoundJson()
    }
  }

  def insertProject(): Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    Try((request.body \ "name").as[String]) match {
      case Success(n) => repository.insertProject(n).map {
        case Success(_) => Ok
        case Failure(e) => BadRequestJson(e.getMessage)
      }
      case Failure(e) => Future.successful(BadRequestJson(e.getMessage))
    }
  }

  def updateProject(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    Try((request.body \ "name").as[String]) match {
      case Success(n) => repository.updateProject(id, n).map {
        case Success(_) => Ok
        case Failure(e) => BadRequestJson(e.getMessage)
      }
      case Failure(e) => Future.successful(BadRequestJson(e.getMessage))
    }
  }
  def softDeleteProject(id: Int): Action[AnyContent] = Action.async { implicit request =>
    val deletedProject = repository.softDeleteProject(id)

    deletedProject.map {
      case 0 => NotFoundJson()
      case _ => Ok
    }
  }
}

