import repositories.{ProjectRepository, TaskRepository}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.TryValues._
import java.sql.Timestamp

class TaskRepositorySpec extends PlaySpec with GuiceOneAppPerSuite {

  "Task repository" should {

    val app2taskRepository = Application.instanceCache[TaskRepository]
    val taskRepository: TaskRepository = app2taskRepository(app)

    val app2projectRepository = Application.instanceCache[ProjectRepository]
    val projectRepository: ProjectRepository = app2projectRepository(app)

    val projectName = "projectName"

    "have working project repository" in {
      projectRepository.insertProject(projectName).futureValue.success.value shouldBe 1
      projectRepository.findProjectWithTasks(1).futureValue shouldBe Symbol("defined")
    }

    "add a new task to project without optional parameters" in {
      taskRepository.insertTask(1, 4, None, None).futureValue.success.value shouldBe 1
    }

    "add a new task to project with optional parameters" in {
      taskRepository.insertTask(1, 4, Some(4), Some("testTask")).futureValue.success.value shouldBe 1
    }

    "decline to add task with wrong project id" in {
      taskRepository.insertTask(2, 4, None, None).futureValue.success.value shouldBe 0
    }

    "update task" in {
      taskRepository.updateTask(1, 1, Timestamp.valueOf("0001-01-01 00:00:00"), 4, None, None).futureValue.success.value shouldBe 1
    }

    "decline to update task with wrong id" in {
      taskRepository.updateTask(777, 1, Timestamp.valueOf("0001-01-01 00:00:00"), 4, None, None).futureValue.success.value shouldBe 0
    }

    "decline to update task with wrong project id" in {
      taskRepository.updateTask(1, 777, Timestamp.valueOf("0001-01-01 00:00:00"), 4, None, None).futureValue.success.value shouldBe 0
    }

    "delete task" in {
      taskRepository.softDeleteTask(2).futureValue shouldBe 1
    }

    "decline to delete already deleted task" in {
      taskRepository.softDeleteTask(2).futureValue shouldBe 0
    }

    "decline to delete no task with wrong id" in {
      taskRepository.softDeleteTask(777).futureValue shouldBe 0
    }

    "add no task with duration collision" in {
      taskRepository.insertTask(1, 400000000, None, None).futureValue.success.value shouldBe 1
      taskRepository.insertTask(1, 4, None, None).futureValue.failure.exception shouldBe a [IllegalArgumentException]
    }

    "update no task with duration collision" in {
      taskRepository.updateTask(1, 1, Timestamp.valueOf("0001-01-01 00:00:00"), 4, None, None).futureValue.failure.exception shouldBe a [IllegalArgumentException]
    }

  }
}
