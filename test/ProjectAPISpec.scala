import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ProjectAPISpec extends PlaySpec with GuiceOneAppPerSuite {

  val selectAllProjectsURL = s"/v1/projects"
  def selectProjectURL(name: String) = s"/v1/projects/$name"
  def addProjectURL(name: String) = s"/v1/projects/add/$name"
  def updateProjectULR(oldName: String, newName: String) = s"/v1/projects/$oldName/update/$newName"
  def deleteProjectURL(name: String) = s"/v1/projects/delete/$name"

  "ProjectAPI" should {

    val projectName = "apiTest001"

    "add a project" in {
      val response = route(app, FakeRequest(POST, addProjectURL(projectName))).get

      status(response) mustBe OK
    }

    "show added project" in {
      val response = route(app, FakeRequest(GET, selectProjectURL(projectName))).get

      status(response) mustBe OK
      contentAsString(response) must include (projectName)
    }

    "show all projects" in {
      val response = route(app, FakeRequest(GET, selectAllProjectsURL)).get

      status(response) mustBe OK
      contentAsString(response) must include (projectName)
    }

    "update project name" in {
      val projectNewName = "apiTest001newName"

      val response = route(app, FakeRequest(PUT, updateProjectULR(projectName, projectNewName))).get

      status(response) mustBe OK
    }

    "delete project" in {
      val projectNewName = "apiTest001newName"

      val response = route(app, FakeRequest(DELETE, deleteProjectURL(projectNewName))).get

      status(response) mustBe OK
    }

  }
}
