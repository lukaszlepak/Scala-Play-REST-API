package v1.project

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

class ProjectController @Inject()(repository: ProjectRepository, cc: ControllerComponents) extends AbstractController(cc) {

  def selectAll(): Action[AnyContent] = Action.async {
    val futureProjects = repository.selectAll

    for (
      projects <- futureProjects
    ) yield Ok(Json.toJson(projects))
  }

  def select(name: String): Action[AnyContent] = Action.async {
    val futureProject = repository.select(name)

    futureProject.map {
      case Some(p) => Ok(Json.toJson(p))
      case None => NotFound("NO RECORD WITH THAT NAME")
    }
  }

  def insert(name: String): Action[AnyContent] = Action.async {
    val ts = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))

    val addedProject = repository.insert(name, ts)

    addedProject.map {
      case -1 => BadRequest("THERE EXISTS PROJECT WITH THAT NAME")
      case _ => Ok
    }
  }

  def update(oldName: String, newName: String): Action[AnyContent] = Action.async {
    val updatedProject = repository.update(oldName, newName)

    updatedProject.map {
      case -1 => BadRequest("THERE EXISTS PROJECT WITH THAT NAME")
      case 0 => NotFound("NO RECORD WITH THAT NAME")
      case _ => Ok
    }
  }
  def softDelete(name: String): Action[AnyContent] = Action.async {
    val ts = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))

    val deletedProject = repository.softDelete(name, ts)

    deletedProject.map {
      case 0 => NotFound("NO RECORD WITH THAT NAME")
      case _ => Ok
    }
  }
}

