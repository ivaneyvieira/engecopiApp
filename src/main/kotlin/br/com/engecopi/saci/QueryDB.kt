package br.com.engecopi.saci

import br.com.engecopi.utils.SystemUtils
import br.com.engecopi.utils.SystemUtils.readFile
import org.sql2o.Connection
import org.sql2o.Query
import org.sql2o.Sql2o
import org.sql2o.StatementRunnableWithResult
import org.sql2o.converters.Converter
import org.sql2o.quirks.NoQuirks
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass

typealias QueryHandle = Query.() -> Unit

open class QueryDB(private val driver: String, val url: String, val username: String, val password: String) {
  private val sql2o: Sql2o

  init {
    registerDriver(driver)
    val maps = mapConverter()
    this.sql2o = Sql2o(url, username, password, NoQuirks(maps))
  }

  open fun mapConverter(): Map<Class<*>, Converter<*>> {
    val maps = HashMap<Class<*>, Converter<*>>()
    maps[LocalDate::class.java] = LocalDateConverter()
    maps[LocalTime::class.java] = LocalSqlTimeConverter()
    maps[ByteArray::class.java] = ByteArrayConverter()
    return maps
  }

  fun <V> withConnection(runnable: StatementRunnableWithResult): V = sql2o.withConnection(runnable)

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
    file: String,
    vararg params: Pair<String, String>,
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

  /*********************************************************************************/
  fun Query.addOptionalParameter(name: String, value: String?): Query {
    if (this.paramNameToIdxMap.containsKey(name)) this.addParameter(name, value)
    return this
  }

  fun Query.addOptionalParameter(name: String, value: ByteArray?): Query {
    if (this.paramNameToIdxMap.containsKey(name)) this.addParameter(name, value)
    return this
  }

  fun Query.addOptionalParameter(name: String, value: Int): Query {
    if (this.paramNameToIdxMap.containsKey(name)) this.addParameter(name, value)
    return this
  }

  fun Query.addOptionalParameter(name: String, value: Double): Query {
    if (this.paramNameToIdxMap.containsKey(name)) this.addParameter(name, value)
    return this
  }

  protected fun <T : Any> query(file: String, classes: KClass<T>, lambda: QueryHandle = {}): List<T> {
    val statements = toStratments(file)
    if (statements.isEmpty()) return emptyList()
    val lastIndex = statements.lastIndex
    val query = statements[lastIndex]
    val updates = if (statements.size > 1) statements.subList(0, lastIndex) else emptyList()
    return transaction { con ->
      scriptSQL(con, updates, lambda)
      val ret: List<T> = querySQL(con, query, classes, lambda)
      ret
    }
  }

  private fun <T : Any> querySQL(con: Connection, sql: String?, classes: KClass<T>, lambda: QueryHandle = {}): List<T> {
    val query = con.createQuery(sql)
    query.lambda()
    println(sql)
    return query.executeAndFetch(classes.java)
  }

  fun toStratments(file: String): List<String> {
    return if (file.startsWith("/")) readFile(file).split(";").filter { it.isNotBlank() || it.isNotEmpty() }
    else listOf(file)
  }

  private fun <T> transaction(block: (Connection) -> T): T {
    return sql2o.beginTransaction().use { con ->
      val ret = block(con)
      con.commit()
      ret
    }
  }

  private fun scriptSQL(con: Connection, stratments: List<String>, lambda: QueryHandle = {}) {
    stratments.forEach { sql ->
      val query = con.createQuery(sql)
      query.lambda()
      query.executeUpdate()
      println(sql)
    }
  }
}
