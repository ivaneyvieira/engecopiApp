package br.com.engecopi.app.model

import br.com.engecopi.saci.saci
import com.vaadin.icons.VaadinIcons
import com.vaadin.server.Resource

data class FiltroPedido(
  var tipoMov: TipoMov? = TipoMov.SAIDA,
  var tipoNota: TipoNota? = TipoNota.PERDA,
  var loja: Loja? = Loja.JS,
  var numPedido: String? = null
                       ) {
  fun findPedido() = saci.pedidoNota(loja, numPedido)
}

enum class TipoMov(
  val cod: String, val descricao: String, val operacao: String, val icon: Resource
                  ) {
  SAIDA("S", "Saída", "saida", VaadinIcons.OUTBOX),
  ENTRADA("E", "Entrada", "entrada", VaadinIcons.INBOX)
}

enum class Loja(val numero: Int, val descricao: String) {
  JS(1, "JS"),
  DS(2, "DS"),
  MR(3, "MR"),
  MF(4, "MF"),
  PK(5, "PK"),
  AD(10, "ADM");

  companion object {
    fun findLoja(storeno: Int?): Loja? = Loja.values().toList().firstOrNull {
      it.numero == storeno
    }
  }
}

enum class TipoNota(val numero: Int, val descricao: String) {
  GARANTIA(7, "Garantia"),
  PERDA(9, "Perda")
}