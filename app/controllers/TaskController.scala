package controllers

import java.sql.Timestamp

import javax.inject.Inject
import jsonErrors.JSONError
import play.api.libs.json.JsValue
import play.api.mvc._
import repositories.TaskRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class TaskController @Inject()(repository: TaskRepository, cc: ControllerComponents) extends AbstractController(cc) with JSONError {

  def insertTask(): Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>

    val params = for {
      project_id <- Try((request.body \ "project_id").as[Int])
      duration <- Try((request.body \ "duration").as[Long])
    } yield (project_id, duration)

    params match {
      case Success(value) => {
        repository.insertTask(value._1, value._2, (request.body \ "volume").asOpt[Int], (request.body \ "description").asOpt[String])
          .map {
            case Success(res) => res match {
              case 0 => NotFoundJson()
              case _ => Ok
            }
            case Failure(e: IllegalArgumentException) => BadRequestJson(e.getMessage)
          }
      }
      case Failure(e) => Future.successful(BadRequestJson(e.getMessage))
    }
  }

  def updateTask(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>

    val params = for {
      project_id <- Try((request.body \ "project_id").as[Int])
      timeStamp <- Try(Timestamp.valueOf((request.body \ "timestamp").as[String]))
      duration <- Try((request.body \ "duration").as[Long])
    } yield (project_id, timeStamp, duration)

    params match {
      case Success(value) => {
        repository.updateTask(id, value._1, value._2, value._3, (request.body \ "volume").asOpt[Int], (request.body \ "description").asOpt[String])
          .map {
            case Success(res) => res match {
              case 0 => NotFoundJson()
              case _ => Ok
            }
            case Failure(e: IllegalArgumentException) => BadRequestJson(e.getMessage)
          }
      }
      case Failure(e) => Future.successful(BadRequestJson(e.getMessage))
    }
  }

  def softDeleteTask(id: Int): Action[AnyContent] = Action.async { implicit request =>
    val deletedTask = repository.softDeleteTask(id)

    deletedTask.map {
      case 0 => NotFoundJson()
      case _ => Ok
    }
  }
}
