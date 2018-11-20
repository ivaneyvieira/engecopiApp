package br.com.engecopi.saci.beans

import br.com.engecopi.app.model.Loja
import br.com.engecopi.saci.QuerySaci
import java.util.*

class Pedido(val storeno: Int?, val ordno: Int?, val date: Date?,
             val userno: Int?, val username: String?, val cliente: String?, val status: Int?) {
  fun loja(): Loja? = Loja.values().firstOrNull {
    it.numero == storeno
  }

  fun notaFiscal(tipo: String): NotaFiscal? {
    val query = QuerySaci()
    return query.pesquisaNota(storeno ?: 0, ordno ?: 0, tipo)
  }
}