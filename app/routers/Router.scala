package routers

import controllers.{ProjectController, TaskController}
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class Router @Inject()(projectController: ProjectController, taskController: TaskController) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/projects" ? q_*"id=${ int(ids) }" & q_?"beforets=$beforeTS" & q_?"afterts=$afterTS" & q_?"isDeleted=${ bool(isDeleted) }"
      & q_?"page=${ int(page) }" & q_?"page_size=${ int(page_size) }"
      & q_?"sort_by=$sortBy" & q_?"order=$order") =>
      projectController.findProjectsWithTasks(ids, beforeTS, afterTS, isDeleted, page, page_size, sortBy, order)
    case GET(p"/projects/${ int(id) }") =>
      projectController.findProjectWithTasks(id)
    case POST(p"/projects") =>
      projectController.insertProject()
    case PUT(p"/projects/${ int(id) }") =>
      projectController.updateProject(id)
    case DELETE(p"/projects/${ int(id) }") =>
      projectController.softDeleteProject(id)

    case POST(p"/tasks") =>
      taskController.insertTask()
    case PUT(p"/tasks/${ int(id) }") =>
      taskController.updateTask(id.toInt)
    case DELETE(p"/tasks/${ int(id) }") =>
      taskController.softDeleteTask(id.toInt)
  }

}
