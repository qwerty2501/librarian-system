package dao

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile


abstract class BaseDAO extends HasDatabaseConfigProvider[JdbcProfile]  {
  import profile.api._
  implicit val localDateToDate = MappedColumnType.base[LocalDateTime, Timestamp](
    l => Timestamp.valueOf(l),
    d => d.toLocalDateTime
  )
}
