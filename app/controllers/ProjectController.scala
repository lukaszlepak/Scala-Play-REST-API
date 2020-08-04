package controllers

import java.sql.Timestamp

import javax.inject.Inject
import models.ProjectWithTasks
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import repositories.ProjectRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ProjectController @Inject()(repository: ProjectRepository, cc: ControllerComponents) extends AbstractController(cc) {

  def findProjectsWithTasks(idList: Seq[Int], beforeTS: Option[String], afterTS: Option[String], isDeleted: Option[Boolean], page: Option[Int], pageSize: Option[Int], sortBy: Option[String], order: Option[String]): Action[AnyContent] = Action.async {
    val futureProjectsWithTasks = repository.findProjectsWithTasks(
      if(idList.isEmpty) None else Option(idList),
      beforeTS.map(Timestamp.valueOf),
      afterTS.map(Timestamp.valueOf),
      isDeleted,
      page,
      pageSize,
      sortBy,
      order
    )

    for {
      z <- futureProjectsWithTasks
    } yield Ok(Json.toJson(z.map(t => ProjectWithTasks(t._1.id, t._1.name, t._1.ts, t._1.lastActivity, t._1.isDeleted, t._2))))
  }

  def findProjectWithTasks(id: Int): Action[AnyContent] = Action.async {
    val futureProject = repository.findProjectWithTasks(id)

    futureProject.map {
      case Some(t) => Ok(Json.toJson(ProjectWithTasks(t._1.id, t._1.name, t._1.ts, t._1.lastActivity, t._1.isDeleted, t._2.flatten)))
      case None => NotFound("NO RECORD WITH THAT NAME")
    }
  }

  def insertProject(): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>
    val addedProject = repository.insertProject((request.body \ "name").as[String])

    addedProject.map {
      case -1 => BadRequest("THERE EXISTS PROJECT WITH THAT NAME")
      case _ => Ok
    }
  }

  def updateProject(id: Int): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>
    val updatedProject = repository.updateProject(id, (request.body \ "name").as[String])

    updatedProject.map {
      case -1 => BadRequest("THERE EXISTS PROJECT WITH THAT NAME")
      case 0 => NotFound("NO RECORD WITH THAT ID")
      case _ => Ok
    }
  }
  def softDeleteProject(id: Int): Action[AnyContent] = Action.async {
    val deletedProject = repository.softDeleteProject(id)

    deletedProject.map {
      case 0 => NotFound("NO RECORD WITH THAT ID")
      case _ => Ok
    }
  }
}

