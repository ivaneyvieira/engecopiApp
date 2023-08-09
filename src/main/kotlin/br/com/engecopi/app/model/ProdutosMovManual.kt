package br.com.engecopi.app.model

import br.com.engecopi.saci.saci

class ProdutosMovManual(
  var loja: Int,
  var prdno: String,
  var grade: String,
  var descricao: String,
  var fornecedor: Long,
  var centrodelucro: String,
  var loc: String,
  var tipo: Long,
  var saldoTotal: Int,
  var saldo: Int,
  var qtty: Int,
  var custo: Double,
  var obs: String,
) {
  val total
    get() = qtty * custo

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProdutosMovManual

    if (loja != other.loja) return false
    if (prdno != other.prdno) return false
    if (grade != other.grade) return false

    return true
  }

  override fun hashCode(): Int {
    var result = loja
    result = 31 * result + prdno.hashCode()
    result = 31 * result + grade.hashCode()
    return result
  }

  fun updateSaldo() {
    val produto = saci.buscaProdutos(BaseProduto(loja = loja, codprd = prdno)).firstOrNull {
      it.grade == grade
    } ?: return
    this.saldo = produto.saldo
  }
}