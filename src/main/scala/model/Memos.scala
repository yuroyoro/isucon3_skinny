package model

import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import org.joda.time.{ DateTime }

case class Memos(
    id: Int,
    user: Int,
    content: Option[String] = None,
    username: Option[String] = None,
    contentHtml: Option[String] = None,
    isPrivate: Byte,
    createdAt: DateTime,
    updatedAt: DateTime) {

  def save()(implicit session: DBSession = Memos.autoSession): Memos = Memos.save(this)(session)

  def destroy()(implicit session: DBSession = Memos.autoSession): Unit = Memos.destroy(this)(session)

}

object Memos extends SQLSyntaxSupport[Memos] {

  override val tableName = "memos"

  override val columns = Seq("id", "user", "content", "is_private", "created_at", "updated_at")

  def apply(m: ResultName[Memos])(rs: WrappedResultSet): Memos = new Memos(
    id = rs.int(m.id),
    user = rs.int(m.user),
    content = rs.stringOpt(m.content),
    isPrivate = rs.byte(m.isPrivate),
    createdAt = rs.timestamp(m.createdAt).toDateTime,
    updatedAt = rs.timestamp(m.updatedAt).toDateTime
  )

  val m = Memos.syntax("m")

  val autoSession = AutoSession

  def total(implicit session: DBSession = autoSession): Long = {
    countBy(sqls"is_private=0")
  }

  def public(page: Int = 0)(implicit session: DBSession = autoSession): List[Memos] = {
    withSQL {
      select.from(Memos as m).where.append(sqls"is_private=0").append(sqls"ORDER BY created_at DESC, id DESC LIMIT 100 OFFSET ${page * 100}")
    }.map(Memos(m.resultName)).list.apply()
  }

  def findByUser(user_id: Int)(implicit session: DBSession = autoSession): List[Memos] = {
    withSQL {
      select.from(Memos as m).where.eq(m.user, user_id).append(sqls"ORDER BY created_at DESC")
    }.map(Memos(m.resultName)).list.apply()
  }

  def findPublicByUser(user_id: Int)(implicit session: DBSession = autoSession): List[Memos] = {
    withSQL {
      select.from(Memos as m).where.eq(m.user, user_id).and.eq(m.isPrivate, 0).append(sqls"ORDER BY created_at DESC")
    }.map(Memos(m.resultName)).list.apply()
  }

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Memos] = {
    withSQL {
      select.from(Memos as m).where.eq(m.id, id)
    }.map(Memos(m.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Memos] = {
    withSQL(select.from(Memos as m)).map(Memos(m.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Memos as m)).map(rs => rs.long(1)).single.apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Memos] = {
    withSQL {
      select.from(Memos as m).where.append(sqls"${where}")
    }.map(Memos(m.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(Memos as m).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }

  def create(
    user: Int,
    content: Option[String] = None,
    isPrivate: Byte,
    createdAt: DateTime,
    updatedAt: DateTime)(implicit session: DBSession = autoSession): Memos = {
    val generatedKey = withSQL {
      insert.into(Memos).columns(
        column.user,
        column.content,
        column.isPrivate,
        column.createdAt,
        column.updatedAt
      ).values(
          user,
          content,
          isPrivate,
          createdAt,
          updatedAt
        )
    }.updateAndReturnGeneratedKey.apply()

    Memos(
      id = generatedKey.toInt,
      user = user,
      content = content,
      isPrivate = isPrivate,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def save(entity: Memos)(implicit session: DBSession = autoSession): Memos = {
    withSQL {
      update(Memos as m).set(
        m.id -> entity.id,
        m.user -> entity.user,
        m.content -> entity.content,
        m.isPrivate -> entity.isPrivate,
        m.createdAt -> entity.createdAt,
        m.updatedAt -> entity.updatedAt
      ).where.eq(m.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Memos)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Memos).where.eq(column.id, entity.id) }.update.apply()
  }

}
