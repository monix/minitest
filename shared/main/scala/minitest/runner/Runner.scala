package minitest.runner

import sbt.testing.{Runner => BaseRunner, Task => BaseTask, _}

final class Runner(
  val args: Array[String],
  val remoteArgs: Array[String],
  classLoader: ClassLoader)
  extends BaseRunner {

  def done(): String = ""

  def tasks(list: Array[TaskDef]): Array[BaseTask] = {
    list.map(t => new Task(t, classLoader))
  }

  def receiveMessage(msg: String): Option[String] = {
    None
  }

  def serializeTask(task: BaseTask, serializer: (TaskDef) => String): String =
    serializer(task.taskDef())

  def deserializeTask(task: String, deserializer: (String) => TaskDef): BaseTask =
    new Task(deserializer(task), classLoader)
}
