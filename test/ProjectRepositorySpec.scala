import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import repositories.ProjectRepository
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.TryValues._

class ProjectRepositorySpec extends PlaySpec with GuiceOneAppPerSuite {

  "Project repository" should {

    val app2repository = Application.instanceCache[ProjectRepository]
    val repository: ProjectRepository = app2repository(app)

    val projectName = "test1"
    val updatedProjectName = "test1NewName"
    val notUniqueName = "test1NotUnique"


    "be empty on init" in {
      repository.findProjectsWithTasks(None, None, None, None, None, None, None, None).futureValue shouldBe empty
    }

    "insert a new project" in {
      repository.insertProject(projectName).futureValue.success.value shouldBe 1
    }

    "decline to insert a new project with used name" in {
      repository.insertProject(projectName).futureValue.failure.exception shouldBe a [JdbcSQLIntegrityConstraintViolationException]
    }

    "find all projects" in {
      repository.findProjectsWithTasks(None, None, None, None, None, None, None, None).futureValue should not be empty
    }

    "find project" in {
      val foundProject = repository.findProjectWithTasks(1).futureValue

      foundProject shouldBe Symbol("defined")
      foundProject.get._1.name shouldBe projectName
    }

    "update project" in {
      repository.updateProject(1, updatedProjectName).futureValue.success.value shouldBe 1
    }

    "update no project with wrong id" in {
      repository.updateProject(2, updatedProjectName).futureValue.success.value shouldBe 0
    }

    "decline to update projects with not unique name" in {
      repository.insertProject(notUniqueName)

      repository.updateProject(1, notUniqueName).futureValue.failure.exception shouldBe a [JdbcSQLIntegrityConstraintViolationException]
    }

    "soft delete project" in {
      repository.softDeleteProject(1).futureValue shouldBe 1
    }

    "soft delete no project with wrong id" in {
      repository.softDeleteProject(2).futureValue shouldBe 0
    }
  }

}
