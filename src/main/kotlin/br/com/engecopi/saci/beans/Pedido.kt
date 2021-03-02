package br.com.engecopi.saci.beans

import br.com.engecopi.app.model.Loja
import br.com.engecopi.saci.saci
import java.util.*
import java.util.concurrent.TimeUnit

class Pedido(
        val storeno: Int?,
        val numero: String?,
        val date: Date?,
        val userno: Int?,
        val username: String?,
        val cpf_cgc: String?,
        val cliente: String?,
        val status: Int?,
        val tipo: String?,
        val storeno_custno: Int
            ) {
  val loja
    get() =
      Loja.values()
        .firstOrNull { it.numero == storeno }
  val numeroPedido
    get() = numero?.split("/")
      ?.getOrNull(0)
  val serie
    get() = numero?.split("/")
      ?.getOrNull(1)

  fun notaFiscal(tipo: String): NotaFiscal? {
    return saci.pesquisaNotaSTKMOV(storeno, numeroPedido, tipo)
  }

  fun isEngecopi() = cliente?.contains("ENGECOPI", ignoreCase = true) ?: false

  fun produtos() = saci.pedidoProduto(loja?.numero, numero)

  fun produtoValido() = produtos().any { (it.prdno ?: "000000") < "980001" }

  fun isDataValida(): Boolean {
    val datePedido = date ?: return false
    val dateNow = Date()
    val diff = dateNow.time - datePedido.time
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    return days < 30
  }

  fun isLojaValida(): Boolean {
    //    return storeno == storeno_custno
    return storeno == storeno
  }
}