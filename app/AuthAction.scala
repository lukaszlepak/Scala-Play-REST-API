package actions

import java.util.UUID

import play.api.mvc.{ActionBuilderImpl, BodyParsers, Request, Result}
import javax.inject.Inject
import jsonErrors.JSONError
import pdi.jwt.{JwtAlgorithm, JwtJson}
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AuthAction @Inject()(configuration: play.api.Configuration, parser: BodyParsers.Default, repository: UserRepository)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) with JSONError{
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    implicit val req = request

    val secretKey = configuration.underlying.getString("jwt.secret")

    val token = request.headers.get("Authorization").getOrElse("")

    val user_UUID = for {
      t <- JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256))
      s <- Try((t \ "user").as[String])
      u <- Try(UUID.fromString(s))
    } yield u

    user_UUID match {
      case Success(u) => {
        repository.findUser(u) flatMap {
          case true => block(request)
          case false => Future.successful(UnAuthorizedJson("NO USER WITH THAT UUID"))
        }
      }
      case Failure(_) => Future.successful(UnAuthorizedJson("INVALID USER_UUID IN JWT CLAIM"))
    }
  }
}
