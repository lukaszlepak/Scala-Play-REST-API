import java.sql.Timestamp
import java.time.temporal.ChronoUnit
import java.time.Instant

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.matchers.should.Matchers._
import v1.project.ProjectRepository

import scala.concurrent.Await
import scala.concurrent.duration._


class ProjectRepositorySpec extends PlaySpec with GuiceOneAppPerSuite {
  "ProjectRepository" should {

    val app2repository = Application.instanceCache[ProjectRepository]
    val repository: ProjectRepository = app2repository(app)

    "insert and return new project" in {
      val timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))

      Await.result(repository.insert("test_project007", timestamp), 5.seconds)

      repository.selectAll.futureValue.map(_.name) should contain ("test_project007")
    }

    "select project" in {
      val selectedProject = repository.select("test_project007")

      Await.result(selectedProject, 5.seconds)

      selectedProject.futureValue should be ('defined)
      selectedProject.futureValue.get.name should be ("test_project007")
    }

    "update name in project" in {
      Await.result(repository.update("test_project007", "test_project007newName"), 5.seconds)

      repository.selectAll.futureValue.map(_.name) should contain ("test_project007newName")
    }

    "softDelete project by name" in {
      val timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))

      Await.result(repository.softDelete("test_project007newName", timestamp), 5.seconds)

      repository.select("test_project007newName").futureValue.map(_.isDeleted) should not contain Timestamp.valueOf("0001-01-01 00:00:00")
    }
  }
}
