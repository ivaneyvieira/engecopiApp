package br.com.engecopi.saci.beans

class NotaFiscal(val tipo: String?, val numero: String?, val serie: String?, val cancelado: Boolean?) {
  val tipoDescricao
    get() = when (tipo) {
      "E" -> "Entrada"
      "S" -> "Saída"
      else -> ""
    }
}