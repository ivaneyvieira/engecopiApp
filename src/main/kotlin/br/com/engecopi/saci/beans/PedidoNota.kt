package br.com.engecopi.saci.beans

import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.saci.beans.TipoPedido.PEDIDO
import br.com.engecopi.saci.saci
import java.util.*
import java.util.concurrent.TimeUnit

class PedidoNota(val storeno: Int?,
                 val numero: String?,
                 val date: Date?,
                 val userno: Int?,
                 val username: String?,
                 val cpf_cgc: String?,
                 val cliente: String?,
                 val status: StatusPedido?,
                 val tipo: TipoPedido?,
                 val storeno_custno: Int) {
  val loja
    get() = Loja.findLoja(storeno)
  val numeroPedido
    get() = numero?.split("/")?.getOrNull(0)
  val serie
    get() = numero?.split("/")?.getOrNull(1)

  val tipoEntradaSaida
    get() = tipo?.descricao ?: ""

  fun notaFiscal(tipo: TipoMov?, tipoNota: TipoNota?): NotaFiscal? {
    val loja = Loja.findLoja(storeno)
    return saci.pesquisaNotaSTKMOV(loja, numeroPedido, tipo, tipoNota)
  }

  fun isEngecopi() = cliente?.contains("ENGECOPI", ignoreCase = true) ?: false

  fun produtos() = saci.pedidoProduto(loja?.numero, numero)

  fun produtoValido() = produtos().any { (it.prdno ?: "000000") < "980001" }

  fun isDataValida(): Boolean {
    if (tipo != PEDIDO) return true
    else {
      val datePedido = date ?: return false
      val dateNow = Date()
      val diff = dateNow.time - datePedido.time
      val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
      return days < 366
    }
  }

  fun isData30Dias(): Boolean {
    if (tipo != PEDIDO) return true
    else {
      val datePedido = date ?: return false
      val dateNow = Date()
      val diff = dateNow.time - datePedido.time
      val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
      return days < 30
    }
  }

  fun isLojaValida(): Boolean {
    return storeno == storeno
  }
}

enum class TipoPedido(val text: String, val descricao: String) {
  PEDIDO("PEDIDO", "Pedido de venda"),
  DEVOLUCAO("DEVOLUCAO", "Saída por Devolução"),
  COMPRA("COMPRA", "Entrada por " + "Compra")
}

enum class StatusPedido(val num: Int) {
  NAO_PROCESSADO(1), JA_PROCESSADO(4)
}