package models

import java.sql.Timestamp

import play.api.libs.json._

case class ProjectWithTasks(id: Int, name: String, ts: Timestamp, lastActivity: Timestamp, isDeleted: Option[Timestamp], tasks: Seq[Task] = Seq())

object ProjectWithTasks {
  def timestampToString(t: Timestamp): String = t.toString
  def stringToTimestamp(dt: String): Timestamp = Timestamp.valueOf(dt)

  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def writes(t: Timestamp): JsValue = Json.toJson(timestampToString(t))
    def reads(json: JsValue): JsResult[Timestamp] = Json.fromJson[String](json).map(stringToTimestamp)
  }
  
  implicit val format: Format[ProjectWithTasks] = Json.format
}
