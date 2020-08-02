package controllers

import javax.inject.Inject
import models.ProjectWithTasks
import play.api.libs.json.Json
import play.api.mvc._
import repositories.ProjectRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ProjectController @Inject()(repository: ProjectRepository, cc: ControllerComponents) extends AbstractController(cc) {

  def findProjectsWithTasks: Action[AnyContent] = Action.async {
    val futureProjectsWithTasks = repository.findProjectsWithTasks()

    for {
      z <- futureProjectsWithTasks
    } yield Ok(Json.toJson(z.map(t => ProjectWithTasks(t._1.id, t._1.name, t._1.ts, t._1.isDeleted, t._2.flatten))))
  }

  def findProjectWithTasks(name: String): Action[AnyContent] = Action.async {
    val futureProject = repository.findProjectWithTasks(name)

    futureProject.map {
      case Some(t) => Ok(Json.toJson(ProjectWithTasks(t._1.id, t._1.name, t._1.ts, t._1.isDeleted, t._2.flatten)))
      case None => NotFound("NO RECORD WITH THAT NAME")
    }
  }

  def insertProject(name: String): Action[AnyContent] = Action.async {
    val addedProject = repository.insertProject(name)

    addedProject.map {
      case -1 => BadRequest("THERE EXISTS PROJECT WITH THAT NAME")
      case _ => Ok
    }
  }

  def updateProject(oldName: String, newName: String): Action[AnyContent] = Action.async {
    val updatedProject = repository.updateProject(oldName, newName)

    updatedProject.map {
      case -1 => BadRequest("THERE EXISTS PROJECT WITH THAT NAME")
      case 0 => NotFound("NO RECORD WITH THAT NAME")
      case _ => Ok
    }
  }
  def softDeleteProject(name: String): Action[AnyContent] = Action.async {
    val deletedProject = repository.softDeleteProject(name)

    deletedProject.map {
      case 0 => NotFound("NO RECORD WITH THAT NAME")
      case _ => Ok
    }
  }
}

