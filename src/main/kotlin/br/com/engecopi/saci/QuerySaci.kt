package br.com.engecopi.saci

import br.com.consutec.dao.GestorDADOS
import br.com.consutec.modelo.Base
import br.com.engecopi.saci.beans.AjusteInventario
import br.com.engecopi.saci.beans.DatasProcessamento
import br.com.engecopi.saci.beans.Inventario
import br.com.engecopi.saci.beans.NotaFiscal
import br.com.engecopi.saci.beans.Pedido
import br.com.engecopi.saci.beans.PedidoProduto
import br.com.engecopi.saci.beans.SaldoKardec
import br.com.engecopi.saci.beans.UserSenha
import br.com.engecopi.utils.DB
import br.com.engecopi.utils.lpad
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuerySaci: QueryDB(driver, url, username, password) {
  fun userSenha(login: String): UserSenha? {
    val sql = "/sql/userSenha.sql"
    return query(sql) {q ->
      q.addParameter("login", login).executeAndFetchFirst(UserSenha::class.java)
    }
  }
  
  fun pedidoNota(storeno: Int, numero: String): Pedido? {
    val sql = "/sql/pedido.sql"
    val num = numero.split("/").getOrNull(0) ?: ""
    val serie = numero.split("/").getOrNull(1) ?: ""
    return query(sql) {q ->
      q.addParameter("storeno", storeno).addParameter("numero", num).addParameter("serie", serie)
        .executeAndFetchFirst(Pedido::class.java)
    }
  }
  
  fun pedidoProduto(storeno: Int?, numero: String?): List<PedidoProduto> {
    storeno ?: return emptyList()
    numero ?: return emptyList()
    val sql = "/sql/pedidoProduto.sql"
    val num = numero.split("/").getOrNull(0) ?: ""
    val serie = numero.split("/").getOrNull(1) ?: ""
    return query(sql) {q ->
      q.addParameter("storeno", storeno).addParameter("numero", num).addParameter("serie", serie)
        .executeAndFetch(PedidoProduto::class.java)
    }
  }
  
  fun processaPedido(storeno: Int, numero: String, tipo: String, tipo_nota: Int) {
    val sql = "/sql/processaPedido.sql"
    execute(sql,
            Pair("storeno", "$storeno"),
            Pair("ordno", numero),
            Pair("tipo", "'$tipo'"),
            Pair("t_nota", "$tipo_nota"))
  }
  
  fun processaDevolucao(storeno: Int, nfno: String, nfse: String) {
    val sql = "/sql/processaDevolucao.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("nfno", nfno), Pair("nfse", "'$nfse'"))
  }
  
  fun desfazPedido(storeno: Int, numero: String, tipo: String) {
    val sql = "/sql/desfazPedido.sql"
    
    execute(sql, Pair("storeno", "$storeno"), Pair("ordno", numero), Pair("tipo", "'$tipo'"))
  }
  
  fun desfazDevolucao(storeno: Int, nfno: String, nfse: String) {
    val sql = "/sql/desfazDevolucao.sql"
    
    execute(sql, Pair("storeno", "$storeno"), Pair("nfno", nfno), Pair("nfse", "'$nfse'"))
  }
  
  fun saldoKardec(dataInicial: LocalDate, dataFinal: LocalDate, monitor: (String, Int, Int) -> Unit) {
    val sql = "/sql/saldoKardec.sql"
    val sdf = DateTimeFormatter.ofPattern("yyyyMMdd")
    val di = dataInicial.format(sdf)
    val df = dataFinal.format(sdf)
    execute(sql, Pair("dataInicial", di), Pair("dataFinal", df), monitor = monitor)
  }
  
  fun pesquisaNota(storeno: Int?, numero: String?, tipo: String): NotaFiscal? {
    storeno ?: return null
    numero ?: return null
    val sql = "/sql/pesquisaNota.sql"
    return query(sql) {q ->
      q.addParameter("storeno", storeno).addParameter("ordno", numero).addParameter("tipo", tipo)
        .executeAndFetchFirst(NotaFiscal::class.java)
    }
  }
  
  fun pesquisaSaldoKardec(): List<SaldoKardec> {
    val sql = "/sql/querySaldoKardec.sql"
    return query(sql) {q ->
      q.executeAndFetch(SaldoKardec::class.java)
    }
  }
  
  fun inventarios(): List<Inventario> {
    val sql = "/sql/inventarios.sql"
    return query(sql) {q ->
      q.executeAndFetch(Inventario::class.java)
    }
  }
  
  fun ajustesInventario(numero: String): List<AjusteInventario> {
    val sql = "/sql/ajustesInventario.sql"
    return query(sql) {q ->
      q.addParameter("numero", numero).executeAndFetch(AjusteInventario::class.java)
    }
  }
  
  fun processaAjuste(numero: String) {
    val sql1 = "/sql/processaAjuste.sql"
    execute(sql1, Pair("numero", numero))
    val sql2 = "novoAjuste.sql"
    execute(sql2)
  }
  
  fun defazAjuste(numero: String) {
    val sql = "/sql/desfazAjuste.sql"
    execute(sql, Pair("numero", numero))
  }
  
  fun novoAjuste() {
    val sql = "/sql/novoAjuste.sql"
    execute(sql) //  processaColetor(data)
  }
  
  fun apagaAjuste(ajuste: AjusteInventario) {
    val sql = "/sql/apagaAjuste.sql"
    execute(sql,
            ("numero" to "'${ajuste.numero}'"),
            ("prdno" to "'${ajuste.prdno.lpad(16, " ")}'"),
            ("grade" to "'${ajuste.grade}'"))
  }
  
  fun findGrades(codigo: String?): List<String> {
    val sql = "/sql/findGrades.sql"
    return query(sql) {q ->
      q.addParameter("codigo", codigo.lpad(16, " "))
      q.executeAndFetch(String::class.java)
    }
  }
  
  fun addProdutoAjuste(loja: Int, codigo: String, grade: String, nota: Int, qtty: Int, data: Int) {
    val sql = "/sql/addProdutoAjuste.sql"
    execute(sql,
            ("loja" to "$loja"),
            ("codigo" to "'$codigo'"),
            ("grade" to "'$grade'"),
            ("nota" to "$nota"),
            ("qtty" to "$qtty"),
            ("data" to "$data"))
  }
  
  fun salvaAjuste(ajuste: AjusteInventario) {
    val sql = "/sql/salvaAjuste.sql"
    execute(sql,
            ("numero" to "'${ajuste.numero}'"),
            ("prdno" to "'${ajuste.prdno.lpad(16, " ")}'"),
            ("grade" to "'${ajuste.grade}'"),
            ("quant" to "${ajuste.inventario}"))
  }
  
  fun datasProcessamento(): DatasProcessamento? {
    val sql = "/sql/datasProcessamento.sql"
    return query(sql) {q ->
      q.executeAndFetch(DatasProcessamento::class.java).firstOrNull()
    }
  }
  
  fun <T> gestorDados(exec: (GestorDADOS) -> T): T {
    return withConnection {con, _ ->
      val dados = GestorDADOS(con.jdbcConnection)
      exec(dados)
    }
  }
  
  fun buscaProdutos(base : Base) = gestorDados{gestor ->
    gestor.listar(base)
  }
  
  companion object {
    val db = DB("saci")
    internal val driver = db.driver
    internal val url = db.url
    internal val username = db.username
    internal val password = db.password
    
    //internal val sqldir = db.sqldir
    val ipServer = db.url.split("/").getOrNull(2)
  }
}

val saci = QuerySaci()
