package br.com.engecopi.saci

import br.com.engecopi.app.model.*
import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.app.model.TipoMov.SAIDA
import br.com.engecopi.saci.DestinoMov.STKMOV
import br.com.engecopi.saci.beans.*
import br.com.engecopi.utils.DB
import br.com.engecopi.utils.lpad
import org.sql2o.converters.Converter
import org.sql2o.converters.ConverterException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuerySaci : QueryDB(driver, url, username, password) {

  override fun mapConverter(): Map<Class<*>, Converter<*>> {
    val map = super.mapConverter()
    val mapSaci = HashMap<Class<*>, Converter<*>>()
    mapSaci.putAll(map)
    mapSaci[StatusPedido::class.java] = StatusPedidoConverter()
    mapSaci[TipoPedido::class.java] = TipoPedidoConverter()
    return mapSaci
  }

  fun pedidoNota(loja: Loja?, numero: String?): PedidoNota? {
    loja ?: return null
    numero ?: return null
    val sql = "/sql/pedido.sql"
    val num = numero.split("/").getOrNull(0) ?: ""
    val serie = numero.split("/").getOrNull(1) ?: ""
    return query(sql) { q ->
      q
          .addParameter("storeno", loja.numero)
          .addParameter("numero", num)
          .addParameter("serie", serie)
          .executeAndFetchFirst(PedidoNota::class.java)
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

  fun processaPedido(storeno: Int, numero: String, tipoMov: TipoMov, tipoNota: TipoNota, destino: DestinoMov) {
    val tipo = tipoMov.cod
    val sql = if (destino == STKMOV) "/sql/processaPedido.sql" else "/sql/processaPedidoNF.sql"
    execute(sql,
        Pair("storeno", "$storeno"),
        Pair("ordno", numero),
        Pair("tipo", "'$tipo'"),
        Pair("t_nota", "${tipoNota.numero}"))
  }

  fun processaNota(storeno: Int, nfno: String, nfse: String, destino: DestinoMov) {
    val sql = if (destino == STKMOV) "/sql/processaDevolucao.sql" else "/sql/processaDevolucaoNF.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("nfno", nfno), Pair("nfse", "'$nfse'"))
  }

  fun desfazPedido(storeno: Int, numero: String, tipoMov: TipoMov, destino: DestinoMov) {
    val tipo = tipoMov.cod
    val sql = if (destino == STKMOV) "/sql/desfazPedido.sql" else "/sql/desfazPedidoNF.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("ordno", numero), Pair("tipo", "'$tipo'"))
  }

  fun desfazNota(storeno: Int, nfno: String, nfse: String, destino: DestinoMov) {
    val sql = if (destino == STKMOV) "/sql/desfazDevolucao.sql" else "/sql/desfazDevolucaoNF.sql"
    execute(sql, Pair("storeno", "$storeno"), Pair("nfno", nfno), Pair("nfse", "'$nfse'"))
  }

  fun saldoKardec(dataInicial: LocalDate, dataFinal: LocalDate, monitor: (String, Int, Int) -> Unit) {
    val sql = "/sql/saldoKardec.sql"
    val sdf = DateTimeFormatter.ofPattern("yyyyMMdd")
    val di = dataInicial.format(sdf)
    val df = dataFinal.format(sdf)
    execute(sql, Pair("dataInicial", di), Pair("dataFinal", df), monitor = monitor)
  }

  fun pesquisaNotaSTKMOV(loja: Loja?, numero: String?, tipo: TipoMov?, status: TipoNota?): NotaFiscal? {
    val storeno = loja?.numero ?: return null
    numero ?: return null
    status ?: return null
    tipo ?: return null
    val sql = "/sql/pesquisaNota.sql"
    return query(sql) { q ->
      q
          .addParameter("storeno", storeno)
          .addParameter("ordno", numero)
          .addParameter("tipo", tipo.cod)
          .addParameter("status", status.numero)
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
    val sql2 = "/sql/novoAjuste.sql"
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
    execute(sql,
        ("numero" to "'${ajuste.numero}'"),
        ("prdno" to "'${ajuste.prdno.lpad(16, " ")}'"),
        ("grade" to "'${ajuste.grade}'"))
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
      addOptionalParameter("numPedido", base.numPedido)
      addOptionalParameter("vends", base.fornecedores)
      addOptionalParameter("types", base.tipos)
      addOptionalParameter("ym", base.mesAno)
    }
  }

  private fun xanoInventario(): Xano? {
    val sql = "/sql/xanoInventario.sql"
    return query(sql, Xano::class).firstOrNull()
  }

  fun executarVoltaStatus(base: Base) {
    val loja = base.lojaDestino
    val numPedido = base.numPedido
    val sql = "/sql/voltaStatus.sql"
    execute(sql, ("loja" to "$loja"), ("numPedido" to "'$numPedido'"))
  }

  fun executarPerda(base: Base): String {
    val produto = buscaProdutos(base)
    val xanoBean = xanoInventario()
    val xano = xanoBean?.xano ?: return ""
    val tipo = base.operacao.cod
    produto.forEach { prd ->
      processaProdutoPerda(xano, tipo, prd, base)
    }
    return xano.toString()
  }

  fun executarGarantia(base: Base): String {
    val produto = buscaProdutos(base)
    val xano = xanoInventario()?.xano ?: return ""
    val tipo = base.operacao.cod
    produto.forEach { prd ->
      processaProdutoGarantia(xano, tipo, prd, base)
    }
    return xano.toString()
  }

  private fun processaProdutoGarantia(xano: Int, tipo: String, prd: Produtos, base: Base) {
    val sql = "/sql/ajustaInventarioGarantia.sql"
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

  private fun processaProdutoPerda(xano: Int, tipo: String, prd: Produtos, base: Base) {
    val sql = "/sql/ajustaInventarioPerda.sql"
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

  fun validarNfSaida(loja: Loja, nota: Int, tipo: TipoNota): Boolean {
    val mov = pesquisaNotaSTKMOV(loja, nota.toString(), SAIDA, tipo)
    return mov != null
  }

  fun validarNfEntrada(loja: Loja, nota: Int, tipo: TipoNota): Boolean {
    val mov = pesquisaNotaSTKMOV(loja, nota.toString(), ENTRADA, tipo)
    return mov != null
  }

  fun desfazerAjuste(loja: Loja, xano: Int, operacao: String, mesAno: Int) {
    val sql = "/sql/removeInventarioPerda.sql"
    execute(
        sql,
        ("tipo" to "'$operacao'"),
        ("xano" to "$xano"),
        ("loja" to "${loja.numero}"),
        ("ym" to "'$mesAno'"),
    )
  }

  fun notaInventario(loja: Loja, xano: Int): NotaInventario? {
    val sql = "/sql/notaInventario.sql"
    return query(sql, NotaInventario::class) {
      addOptionalParameter("loja", loja.numero)
      addOptionalParameter("xano", xano)
    }.firstOrNull()
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

class StatusPedidoConverter : Converter<StatusPedido?> {
  @Throws(ConverterException::class)
  override fun convert(value: Any?): StatusPedido? {
    val num = value?.toString()?.toIntOrNull()

    return StatusPedido.values().firstOrNull { it.num == num }
  }

  override fun toDatabaseParam(value: StatusPedido?): Any? {
    value ?: return null
    return value.num
  }
}

class TipoPedidoConverter : Converter<TipoPedido?> {
  @Throws(ConverterException::class)
  override fun convert(value: Any?): TipoPedido? {
    return TipoPedido.values().firstOrNull { it.text == value }
  }

  override fun toDatabaseParam(value: TipoPedido?): Any? {
    value ?: return null
    return value.text
  }
}

enum class DestinoMov {
  STKMOV, NF
}