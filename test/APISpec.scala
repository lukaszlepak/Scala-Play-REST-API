import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

class APISpec extends PlaySpec with GuiceOneAppPerSuite  {

  val projectsURL = "/v1/projects"
  def projectURL(name: String) = s"/v1/projects/$name"
  def updateProjectULR(oldName: String, newName: String) = s"/v1/projects/$oldName/$newName"

  val tasksURL = "/v1/tasks"
  def taskURL(id: Int) = s"/v1/tasks/$id"

  "API" should {

    val projectName = "apiTest001"
    val projectNewName = "apiTest001newName"

    ////////////PROJECTS

    "add a project" in {
      val response = route(app, FakeRequest(POST, projectURL(projectName))).get
      status(response) mustBe OK
    }

    "show added project" in {
      val response = route(app, FakeRequest(GET, projectURL(projectName))).get
      status(response) mustBe OK
      contentAsString(response) must include (projectName)
    }

    "show all projects" in {
      val response = route(app, FakeRequest(GET, projectsURL)).get
      status(response) mustBe OK
      contentAsString(response) must include (projectName)
    }

    "update project name" in {
      val response = route(app, FakeRequest(PUT, updateProjectULR(projectName, projectNewName))).get
      status(response) mustBe OK
    }

    "delete project" in {
      val response = route(app, FakeRequest(DELETE, projectURL(projectNewName))).get
      status(response) mustBe OK
    }

    ///////////TASKS

    "add a task" in {
      val responseProject = route(app, FakeRequest(GET, projectURL(projectNewName))).get
      status(responseProject) mustBe OK

      val project_id = (contentAsJson(responseProject) \ "id").as[Int]

      val fakeJSON = Json.obj(
        "project_id" -> project_id,
        "duration" -> 1,
        "volume" -> 1,
        "description" -> "test"
      )
      val response = route(app, FakeRequest(POST, tasksURL).withBody(fakeJSON)).get
      status(response) mustBe OK
    }

    "update task" in {
      val responseProject = route(app, FakeRequest(GET, projectURL(projectNewName))).get
      status(responseProject) mustBe OK

      val project_id = (contentAsJson(responseProject) \ "id").as[Int]

      val task_json = (contentAsJson(responseProject) \ "tasks")(0)
      val task_id = (task_json \ "id").as[Int]

      val fakeJSON = Json.obj(
        "project_id" -> project_id,
        "timestamp" -> "2022-07-31 16:55:20.549",
        "duration" -> 2,
        "volume" -> 2,
        "description" -> "test2"
      )
      val response = route(app, FakeRequest(PUT, taskURL(task_id)).withBody(fakeJSON)).get
      status(response) mustBe OK
    }

    "delete task" in {
      val responseProject = route(app, FakeRequest(GET, projectURL(projectNewName))).get
      status(responseProject) mustBe OK

      val task_json = (contentAsJson(responseProject) \ "tasks")(0)
      val task_id = (task_json \ "id").as[Int]

      val response = route(app, FakeRequest(DELETE, taskURL(task_id))).get
      status(response) mustBe OK
    }

  }

}
