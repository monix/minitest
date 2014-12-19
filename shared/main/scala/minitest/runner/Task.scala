package minitest.runner

import minitest.api.{Result, TestSuite}
import org.scalajs.testinterface.TestUtils
import sbt.testing.{Task => BaseTask, _}
import scala.util.Try


final class Task(task: TaskDef, cl: ClassLoader) extends BaseTask {

  def tags(): Array[String] = Array.empty
  def taskDef(): TaskDef = task

  def execute(eventHandler: EventHandler, loggers: Array[Logger],
    continuation: (Array[BaseTask]) => Unit): Unit = {
    continuation(execute(eventHandler, loggers))
  }

  def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[BaseTask] = {
    for (suite <- loadSuite(task.fullyQualifiedName(), cl)) {
      loggers.foreach(_.info(Console.GREEN + task.fullyQualifiedName() + Console.RESET))

      for (property <- suite.properties) {
        val startTS = System.currentTimeMillis()
        val result = property(())
        val endTS = System.currentTimeMillis()

        loggers.foreach(_.info(result.formatted(property.name)))
        eventHandler.handle(event(result, endTS - startTS))
      }
    }

    Array.empty
  }

  def loadSuite(name: String, loader: ClassLoader): Option[TestSuite] = {
    Try(TestUtils.loadModule(name, loader)).toOption
      .collect { case ref: TestSuite => ref }
  }

  def event(result: Result[Unit], durationMillis: Long): Event = new Event {
    def fullyQualifiedName(): String =
      task.fullyQualifiedName()

    def throwable(): OptionalThrowable =
      result match {
        case Result.Exception(source, _) =>
          new OptionalThrowable(source)
        case Result.Failure(_, Some(source), _) =>
          new OptionalThrowable(source)
        case _ =>
          new OptionalThrowable()
      }

    def status(): Status =
      result match {
        case Result.Exception(_,_) =>
          Status.Error
        case Result.Failure(_,_,_) =>
          Status.Failure
        case Result.Success(_) =>
          Status.Success
      }

    def selector(): Selector = {
      task.selectors().head
    }

    def fingerprint(): Fingerprint =
      task.fingerprint()

    def duration(): Long =
      durationMillis
  }
}
