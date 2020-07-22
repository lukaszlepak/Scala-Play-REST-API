package v1.project

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ProjectRouter @Inject()(controller: ProjectController) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/") =>
      controller.selectAll()
    case GET(p"/$name") =>
      controller.select(name)
    case POST(p"/add/$name") =>
      controller.insert(name)
    case PUT(p"/$oldName/update/$newName") =>
      controller.update(oldName, newName)
    case DELETE(p"/delete/$name") =>
      controller.softDelete(name)
  }

}
