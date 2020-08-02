package routers

import controllers.{ProjectController, TaskController}
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class Router @Inject()(projectController: ProjectController, taskController: TaskController) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/projects") =>
      projectController.findProjectsWithTasks
    case GET(p"/projects/$name") =>
      projectController.findProjectWithTasks(name)
    case POST(p"/projects/$name") =>
      projectController.insertProject(name)
    case PUT(p"/projects/$oldName/$newName") =>
      projectController.updateProject(oldName, newName)
    case DELETE(p"/projects/$name") =>
      projectController.softDeleteProject(name)

    case POST(p"/tasks") =>
      taskController.insertTask()
    case PUT(p"/tasks/$id") =>
      taskController.updateTask(id.toInt)
    case DELETE(p"/tasks/$id") =>
      taskController.softDeleteTask(id.toInt)
  }

}
