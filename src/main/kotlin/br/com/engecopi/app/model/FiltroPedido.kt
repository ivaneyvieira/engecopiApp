package br.com.engecopi.app.model

import com.vaadin.icons.VaadinIcons
import com.vaadin.server.Resource

data class FiltroPedido(
        var tipoMov: TipoMov? = TipoMov.SAIDA,
        var loja: Loja? = Loja.JS,
        var numPedido: String? = null
                       )

enum class TipoMov(val cod: String, val descricao: String, val icon: Resource) {
  SAIDA("S", "Saída", VaadinIcons.OUTBOX),
  ENTRADA("E", "Entrada", VaadinIcons.INBOX)
}

enum class Loja(val numero: Int, val descricao: String) {
  JS(1, "José dos Santos e Silva"),
  DS(2, "Dom Severino"),
  MR(3, "Miguel Rosa"),
  MF(4, "Magalhães Filho"),
  PK(5, "Presidente Kennedy"),
  FS(6, "Frei Serafim"),
  NS(7, "Nossa Senhora de Fátima"),
  AD(10, "ADM")
}