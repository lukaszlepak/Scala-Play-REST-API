import javax.inject.{Inject, Provider}
import jsonErrors.JSONError
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.core.SourceMapper

import scala.concurrent._

class ErrorHandler(environment: Environment,
                   configuration: Configuration,
                   sourceMapper: Option[SourceMapper] = None,
                   optionRouter: => Option[Router] = None)
  extends DefaultHttpErrorHandler(environment,
    configuration,
    sourceMapper,
    optionRouter) with JSONError {

  private val logger =
    org.slf4j.LoggerFactory.getLogger("application.ErrorHandler")

  // This maps through Guice so that the above constructor can call methods.
  @Inject
  def this(environment: Environment,
           configuration: Configuration,
           sourceMapper: OptionalSourceMapper,
           router: Provider[Router]) = {
    this(environment,
      configuration,
      sourceMapper.sourceMapper,
      Some(router.get))
  }

  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String): Future[Result] = {
    logger.debug(
      s"onClientError: statusCode = $statusCode, uri = ${request.uri}, message = $message")

    Future.successful {
      StatusJson(statusCode)(message)(request)
    }
  }

  override protected def onDevServerError( request: RequestHeader,
                                           exception: UsefulException): Future[Result] = {
    Future.successful {
      StatusJson(500)(exception.getMessage)(request)
    }
  }

  override protected def onProdServerError( request: RequestHeader,
                                            exception: UsefulException): Future[Result] = {
    Future.successful {
      StatusJson(500)(exception.getMessage)(request)
    }
  }
}