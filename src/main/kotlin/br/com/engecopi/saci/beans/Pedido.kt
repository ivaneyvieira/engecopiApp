package br.com.engecopi.saci.beans

import br.com.engecopi.app.model.Loja
import br.com.engecopi.saci.QuerySaci
import br.com.engecopi.saci.saci
import java.util.*

class Pedido(val storeno: Int?,
             val numero: String?,
             val date: Date?,
             val userno: Int?,
             val username: String?,
             val cpf_cgc: String?,
             val cliente: String?,
             val status: Int?) {
  val loja = Loja.values().firstOrNull { it.numero == storeno }

  fun notaFiscal(tipo: String): NotaFiscal? {
    return saci.pesquisaNota(storeno ?: 0, numero ?: "", tipo)
  }
}