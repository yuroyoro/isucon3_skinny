scalariformSettings

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.24"
)

initialCommands := """
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
"""

