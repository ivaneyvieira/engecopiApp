package br.com.engecopi.saci.beans

import br.com.engecopi.saci.saci

class Inventario(
        var numero: String?,
        var date: Int?,
        var storeno: Int?
                ) {
  fun dataFormat(): String? {
    return date?.let {
      val dateStr = it.toString()
      return dateStr.substring(6, 8) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(0, 4)
    }
  }

  fun processado(): Boolean {
    val ajuste = saci.ajustesInventario(numero ?: "").firstOrNull()
    return ajuste?.let {
      (it.nfEntrada != "") || (it.nfSaida != "")
    } ?: false
  }
}