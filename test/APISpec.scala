import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

class APISpec extends PlaySpec with GuiceOneAppPerSuite {

  val projectsURL = "/v1/projects"
  def projectURL(id: Int) = s"/v1/projects/$id"

  val tasksURL = "/v1/tasks"
  def taskURL(id: Int) = s"/v1/tasks/$id"

  "API" should {

    val projectName = "apiTest001"
    val projectNewName = "apiTest001newName"

    ////////////PROJECTS

    "add a project" in {
      val fakeJSON = Json.obj(
        "name" -> projectName
      )

      val response = route(app, FakeRequest(POST, projectsURL).withBody(fakeJSON)).get
      status(response) mustBe OK
    }

    "show all projects" in {
      val response = route(app, FakeRequest(GET, projectsURL)).get
      status(response) mustBe OK
      contentAsString(response) must include (projectName)
    }

    "show added project" in {
      val response = route(app, FakeRequest(GET, projectURL(1))).get
      status(response) mustBe OK
      contentAsString(response) must include (projectName)
    }

    "update project name" in {
      val fakeJSON = Json.obj(
        "name" -> projectNewName
      )

      val response = route(app, FakeRequest(PUT, projectURL(1)).withBody(fakeJSON)).get
      status(response) mustBe OK
    }

    "delete project" in {
      val response = route(app, FakeRequest(DELETE, projectURL(1))).get
      status(response) mustBe OK
    }

    ///////////TASKS

    "add a task" in {
      val fakeJSON = Json.obj(
        "project_id" -> 1,
        "duration" -> 1,
        "volume" -> 1,
        "description" -> "test"
      )
      val response = route(app, FakeRequest(POST, tasksURL).withBody(fakeJSON)).get
      status(response) mustBe OK
    }

    "update task" in {
      val fakeJSON = Json.obj(
        "project_id" -> 1,
        "timestamp" -> "2022-07-31 16:55:20.549",
        "duration" -> 2,
        "volume" -> 2,
        "description" -> "test2"
      )
      val response = route(app, FakeRequest(PUT, taskURL(1)).withBody(fakeJSON)).get
      status(response) mustBe OK
    }

    "delete task" in {
      val response = route(app, FakeRequest(DELETE, taskURL(1))).get
      status(response) mustBe OK
    }

  }

}
