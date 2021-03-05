package br.com.engecopi.app.model

import com.vaadin.icons.VaadinIcons
import com.vaadin.server.Resource

data class FiltroPedido(var tipoMov: TipoMov? = TipoMov.SAIDA,
                        var tipoNota: Int = 9,
                        var loja: Loja? = Loja.JS,
                        var numPedido: String? = null)

enum class TipoMov(val cod: String,
                   val descricao: String,
                   val operacao: String,
                   val icon: Resource) {
  SAIDA("S", "Saída", "saida", VaadinIcons.OUTBOX),
  ENTRADA("E", "Entrada", "entrada", VaadinIcons.INBOX)
}

enum class Loja(val numero: Int, val descricao: String) {
  JS(1, "JS"),
  DS(2, "DS"),
  MR(3, "MR"),
  MF(4, "MF"),
  PK(5, "PK"),
  AD(10, "ADM")
}