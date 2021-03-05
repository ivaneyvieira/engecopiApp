package br.com.engecopi.saci

import br.com.engecopi.app.model.Base
import br.com.engecopi.app.model.Produtos
import br.com.engecopi.saci.beans.*
import br.com.engecopi.utils.DB
import br.com.engecopi.utils.lpad
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuerySaci : QueryDB(driver, url, username, password) {
  fun pedidoNota(storeno: Int, numero: String): Pedido? {
    val sql = "/sql/pedido.sql"
    val num = numero.split("/").getOrNull(0) ?: ""
    val serie = numero.split("/").getOrNull(1) ?: ""
    return query(sql) { q ->
      q
        .addParameter("storeno", storeno)
        .addParameter("numero", num)
        .addParameter("serie", serie)
        .executeAndFetchFirst(Pedido::class.java)
    }
  }

  fun pedidoProduto(storeno: Int?, numero: String?): List<PedidoProduto> {
    storeno ?: return emptyList()
    numero ?: return emptyList()
    val sql = "/sql/pedidoProduto.sql"
    val num = numero.split("/").getOrNull(0) ?: ""
    val serie = numero.split("/").getOrNull(1) ?: ""
    return query(sql) { q ->
      q
        .addParameter("storeno", storeno)
        .addParameter("numero", num)
        .addParameter("serie", serie)
        .executeAndFetch(PedidoProduto::class.java)
    }
  }

  fun processaPedidoSTKMOV(storeno: Int, numero: String, tipo: String, tipo_nota: Int) {
    val sql = if (tipo_nota == 9) "/sql/processaPedido.sql" else "/sql/processaPedidoNF.sql"
    execute(
      sql,
      Pair("storeno", "$storeno"),
      Pair("ordno", numero),
      Pair("tipo", "'$tipo'"),
      Pair("t_nota", "$tipo_nota")
           )
  }

  fun processaDevolucaoSTKMOV(storeno: Int, nfno: String, nfse: String, tipo_nota: Int) {
    val sql = if (tipo_nota == 9) "/sql/processaDevolucao.sql" else "/sql/processaDevolucaoNF.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("nfno", nfno), Pair("nfse", "'$nfse'"))
  }

  fun desfazPedidoSTKMOV(storeno: Int, numero: String, tipo: String, tipo_nota: Int) {
    val sql = if (tipo_nota == 9) "/sql/desfazPedido.sql" else "/sql/desfazPedidoNF.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("ordno", numero), Pair("tipo", "'$tipo'"))
  }

  fun desfazDevolucaoSTKMOV(storeno: Int, nfno: String, nfse: String, tipo_nota: Int) {
    val sql = if (tipo_nota == 9) "/sql/desfazDevolucao.sql" else "/sql/desfazDevolucaoNF.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("nfno", nfno), Pair("nfse", "'$nfse'"))
  }

  fun saldoKardec(dataInicial: LocalDate,
                  dataFinal: LocalDate,
                  monitor: (String, Int, Int) -> Unit) {
    val sql = "/sql/saldoKardec.sql"
    val sdf = DateTimeFormatter.ofPattern("yyyyMMdd")
    val di = dataInicial.format(sdf)
    val df = dataFinal.format(sdf)
    execute(sql, Pair("dataInicial", di), Pair("dataFinal", df), monitor = monitor)
  }

  fun pesquisaNotaSTKMOV(storeno: Int?, numero: String?, tipo: String): NotaFiscal? {
    storeno ?: return null
    numero ?: return null
    val sql = "/sql/pesquisaNota.sql"
    return query(sql) { q ->
      q
        .addParameter("storeno", storeno)
        .addParameter("ordno", numero)
        .addParameter("tipo", tipo)
        .executeAndFetchFirst(NotaFiscal::class.java)
    }
  }

  fun pesquisaSaldoKardec(): List<SaldoKardec> {
    val sql = "/sql/querySaldoKardec.sql"
    return query(sql) { q ->
      q.executeAndFetch(SaldoKardec::class.java)
    }
  }

  fun inventarios(): List<Inventario> {
    val sql = "/sql/inventarios.sql"
    return query(sql) { q ->
      q.executeAndFetch(Inventario::class.java)
    }
  }

  fun ajustesInventario(numero: String): List<AjusteInventario> {
    val sql = "/sql/ajustesInventario.sql"
    return query(sql) { q ->
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
    val sql = "/sql/apaga Ajuste.sql"
    execute(
      sql,
      ("numero" to "'${ajuste.numero}'"),
      ("prdno" to "'${ajuste.prdno.lpad(16, " ")}'"),
      ("grade" to "'${ajuste.grade}'")
           )
  }

  fun findGrades(codigo: String?): List<String> {
    val sql = "/sql/findGrades.sql"
    return query(sql) { q ->
      q.addParameter("codigo", codigo.lpad(16, " "))
      q.executeAndFetch(String::class.java)
    }
  }

  fun addProdutoAjuste(loja: Int, codigo: String, grade: String, nota: Int, qtty: Int, data: Int) {
    val sql = "/sql/addProdutoAjuste.sql"
    execute(
      sql,
      ("loja" to "$loja"),
      ("codigo" to "'$codigo'"),
      ("grade" to "'$grade'"),
      ("nota" to "$nota"),
      ("qtty" to "$qtty"),
      ("data" to "$data")
           )
  }

  fun salvaAjuste(ajuste: AjusteInventario) {
    val sql = "/sql/salvaAjuste.sql"
    execute(
      sql,
      ("numero" to "'${ajuste.numero}'"),
      ("prdno" to "'${ajuste.prdno.lpad(16, " ")}'"),
      ("grade" to "'${ajuste.grade}'"),
      ("quant" to "${ajuste.inventario}")
           )
  }

  fun datasProcessamento(): DatasProcessamento? {
    val sql = "/sql/datasProcessamento.sql"
    return query(sql) { q ->
      q.executeAndFetch(DatasProcessamento::class.java).firstOrNull()
    }
  }

  fun buscaProdutos(base: Base): List<Produtos> {
    val sql = "/sql/buscaProdutos.sql"
    return query(sql, Produtos::class) {
      addOptionalParameter("loja", base.lojaDestino)
      addOptionalParameter("operacao", base.operacao.operacao)
      addOptionalParameter("prdno", base.codprd)
      addOptionalParameter("vends", base.fornecedores)
      addOptionalParameter("types", base.tipos)
      addOptionalParameter("ym", base.mesAno)
    }
  }

  fun xanoInventario(): Int {
    val sql = "/sql/xanoInventario.sql"
    return query(sql) { q ->
      q.executeScalar(Int::class.java)
    }
  }

  fun executar(base: Base) {
    val produto = buscaProdutos(base)
    val xano = xanoInventario()
    val tipo = base.operacao.cod
    produto.forEach { prd ->
      processaProduto(xano, tipo, prd, base)
    }
  }

  private fun processaProduto(xano: Int, tipo: String, prd: Produtos, base: Base) {
    val sql = "/sql/ajustaInventario.sql"
    execute(
      sql,
      ("tipo" to "'$tipo'"),
      ("xano" to "$xano"),
      ("qttd" to "${(prd.qtdNfForn * 1000).toInt()}"),
      ("custo" to "${(prd.custo * 10000).toInt()}"),
      ("loja" to "${base.lojaDestino}"),
      ("prdno" to "'${prd.prdno}'"),
      ("grade" to "'${prd.grade}'"),
      ("ym" to "'${base.mesAno}'"),
           )
  }

  fun validarNfSaida(loja: Int, nota: Int): Boolean {
    val mov = pesquisaNotaSTKMOV(loja, nota.toString(), "S")
    return mov != null
  }

  fun validarNfEntrada(loja: Int, nota: Int): Boolean {
    val mov = pesquisaNotaSTKMOV(loja, nota.toString(), "E")
    return mov != null
  }

  fun desfazerSaida(loja: Int, nota: Int) {
    TODO()
  }

  fun desfazerEntrada(loja: Int, nota: Int) {
    TODO()
  }

  companion object {
    private val db = DB("saci")
    internal val driver = db.driver
    internal val url = db.url
    internal val username = db.username
    internal val password = db.password

    //internal val sqldir = db.sqldir
    val ipServer = db.url.split("/").getOrNull(2)
  }
}

val saci = QuerySaci()
