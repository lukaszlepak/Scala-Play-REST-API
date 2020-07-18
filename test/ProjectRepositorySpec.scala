import java.sql.Timestamp
import java.time.temporal.ChronoUnit
import java.time.Instant

import models.ProjectRepository
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.matchers.should.Matchers._

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

  }
}
