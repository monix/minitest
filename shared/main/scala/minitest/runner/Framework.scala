package minitest.runner

import sbt.testing.{Framework => BaseFramework, _}

class Framework extends BaseFramework {
  def name(): String = "scala-testkit"

  private[this] object TestKitFingerprint extends SubclassFingerprint {
    val isModule = true
    def requireNoArgConstructor(): Boolean = true
    def superclassName(): String = "minitest.api.TestSuite"
  }

  def fingerprints(): Array[Fingerprint] =
    Array(TestKitFingerprint)


  def slaveRunner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader, send: (String) => Unit): Runner =
    runner(args, remoteArgs, testClassLoader)

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner = {
    val theseArgs = args
    val theseRemoteArgs = remoteArgs

    new Runner {
      def done(): String = ""

      def remoteArgs(): Array[String] = {
        theseRemoteArgs
      }

      def args: Array[String] = {
        theseArgs
      }

      def tasks(list: Array[TaskDef]): Array[Task] = {
        list.map(t => new TaskRunner(t, testClassLoader))
      }

      def receiveMessage(msg: String): Option[String] = {
        None
      }

      def serializeTask(task: Task, serializer: (TaskDef) => String): String =
        serializer(task.taskDef())

      def deserializeTask(task: String, deserializer: (String) => TaskDef): Task =
        new TaskRunner(deserializer(task), testClassLoader)
    }
  }
}
