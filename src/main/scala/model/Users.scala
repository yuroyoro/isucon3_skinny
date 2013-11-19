package model

import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import org.joda.time.{ DateTime }

case class Users(
    id: Int,
    username: String,
    password: String,
    salt: String,
    lastAccess: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Users.autoSession): Users = Users.save(this)(session)

  def destroy()(implicit session: DBSession = Users.autoSession): Unit = Users.destroy(this)(session)

  def updateLastAccess()(implicit session: DBSession = Users.autoSession): Unit = Users.updateLastAccess(this)(session)
}

object Users extends SQLSyntaxSupport[Users] {

  override val tableName = "users"

  override val columns = Seq("id", "username", "password", "salt", "last_access")

  def apply(u: ResultName[Users])(rs: WrappedResultSet): Users = new Users(
    id = rs.int(u.id),
    username = rs.string(u.username),
    password = rs.string(u.password),
    salt = rs.string(u.salt),
    lastAccess = rs.timestampOpt(u.lastAccess).map(_.toDateTime)
  )

  val u = Users.syntax("u")

  val autoSession = AutoSession

  def nameOf(id: Int)(implicit session: DBSession = autoSession): Option[String] = {
    withSQL {
      select(u.username).from(Users as u).where.eq(u.id, id)
    }.map(rs => rs.string(1)).single.apply()
  }

  def findByName(username: String)(implicit session: DBSession = autoSession): Option[Users] = {
    withSQL {
      select.from(Users as u).where.eq(u.username, username)
    }.map(Users(u.resultName)).single.apply()
  }

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Users] = {
    withSQL {
      select.from(Users as u).where.eq(u.id, id)
    }.map(Users(u.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Users] = {
    withSQL(select.from(Users as u)).map(Users(u.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Users as u)).map(rs => rs.long(1)).single.apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Users] = {
    withSQL {
      select.from(Users as u).where.append(sqls"${where}")
    }.map(Users(u.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(Users as u).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }

  def create(
    username: String,
    password: String,
    salt: String,
    lastAccess: Option[DateTime] = None)(implicit session: DBSession = autoSession): Users = {
    val generatedKey = withSQL {
      insert.into(Users).columns(
        column.username,
        column.password,
        column.salt,
        column.lastAccess
      ).values(
          username,
          password,
          salt,
          lastAccess
        )
    }.updateAndReturnGeneratedKey.apply()

    Users(
      id = generatedKey.toInt,
      username = username,
      password = password,
      salt = salt,
      lastAccess = lastAccess)
  }

  def save(entity: Users)(implicit session: DBSession = autoSession): Users = {
    withSQL {
      update(Users as u).set(
        u.id -> entity.id,
        u.username -> entity.username,
        u.password -> entity.password,
        u.salt -> entity.salt,
        u.lastAccess -> entity.lastAccess
      ).where.eq(u.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Users)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Users).where.eq(column.id, entity.id) }.update.apply()
  }

  def updateLastAccess(entity: Users)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      update(Users as u).set(sqls"last_access=now()").where.eq(u.id, entity.id)
    }.update.apply()
  }

}
