package br.com.engecopi.saci

import br.com.engecopi.utils.SystemUtils
import org.sql2o.Connection
import org.sql2o.Query
import org.sql2o.Sql2o

open class QueryDB(
  val driver: String, val url: String, val username: String,
  val password: String
                  ) {
  private val sql2o: Sql2o

  init {
    registerDriver(driver)
    this.sql2o = Sql2o(url, username, password)
  }

  private fun registerDriver(driver: String) {
    try {
      Class.forName(driver)
    } catch (e: ClassNotFoundException) {
      throw RuntimeException(e)
    }
  }

  protected fun <T> query(file: String, lambda: (Query) -> T): T {
    return buildQuery(file) { con, query ->
      val ret = lambda(query)
      con.close()
      ret
    }
  }

  private inline fun <C : AutoCloseable, R> C.trywr(block: (C) -> R): R {
    this.use {
      return block(this)
    }
  }

  protected fun execute(
    file: String, vararg params: Pair<String, String>,
    monitor: (String, Int, Int) -> Unit = { _, _, _ -> }
                       ) {
    var sqlScript = SystemUtils.readFile(file)
    sql2o.beginTransaction().trywr { con ->
      params.forEach { sqlScript = sqlScript.replace(":${it.first}", it.second) }
      val sqls = sqlScript.split(";")
      val count = sqls.size
      sqls.filter { it.trim() != "" }.forEachIndexed { index, sql ->
        println(sql)
        val query = con.createQuery(sql)
        query.executeUpdate()
        val parte = index + 1
        val caption = "Parte $parte/$count"
        monitor(caption, parte, count)
      }
      monitor("", count, count)
      con.commit()
    }
  }

  private fun <T> buildQuery(file: String, proc: (Connection, Query) -> T): T {
    val sql = SystemUtils.readFile(file)
    this.sql2o.open().trywr { con ->
      println(sql)
      val query = con.createQuery(sql)
      return proc(con, query)
    }
  }
}
