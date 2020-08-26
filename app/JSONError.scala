package jsonErrors

import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}

trait JSONError {

  def StatusJson(statusCode: Int)(msg: String)(implicit request: RequestHeader): Result = {
    Results.Status(statusCode)(
      Json.obj(
        "error" -> Json.obj(
          "code" -> statusCode,
          "request_uri" -> request.uri,
          "message" -> msg
        )
      )
    )
  }

  def BadRequestJson(msg: String = "BAD REQUEST")(implicit request: RequestHeader): Result = {
    StatusJson(400)(msg)
  }

  def NotFoundJson(msg: String = "NOT FOUND")(implicit request: RequestHeader): Result = {
    StatusJson(404)(msg)
  }

  def UnAuthorizedJson(msg: String = "NOT AUTHORIZED")(implicit request: RequestHeader): Result = {
    StatusJson(401)(msg)
  }
}
