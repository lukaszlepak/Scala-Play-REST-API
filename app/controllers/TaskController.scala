package controllers

import java.sql.Timestamp

import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc._
import repositories.TaskRepository

import scala.concurrent.ExecutionContext.Implicits.global

class TaskController @Inject()(repository: TaskRepository, cc: ControllerComponents) extends AbstractController(cc) {

  def insertTask(): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>

    val addedTask = repository.insertTask((request.body \ "project_id").as[Int], (request.body \ "duration").as[Long], Option((request.body \ "volume").as[Int]), Option((request.body \ "description").as[String]))

    addedTask.map {
      case 0 => BadRequest("CANNOT ADD NEW TASK, LAST TASK IS NOT FINISHED YET")
      case _ => Ok
    }
  }

  def updateTask(id: Int): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>

    val updatedTask = repository.updateTask(id, (request.body \ "project_id").as[Int], Timestamp.valueOf((request.body \ "timestamp").as[String]), (request.body \ "duration").as[Long], Option((request.body \ "volume").as[Int]), Option((request.body \ "description").as[String]))

    updatedTask.map {
      case -1 => NotFound("NOT FOUND")
      case 0 => BadRequest("CANNOT UPDATE TASK, LAST TASK IS NOT FINISHED YET")
      case _ => Ok
    }
  }

  def softDeleteTask(id: Int): Action[AnyContent] = Action.async {
    val deletedTask = repository.softDeleteTask(id)

    deletedTask.map {
      case 0 => NotFound("NO RECORD WITH THAT NAME")
      case _ => Ok
    }
  }
}
