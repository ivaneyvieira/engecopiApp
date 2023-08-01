package br.com.engecopi.saci.beans

class PedidoProduto(
  val storeno: Int?,
  val numero: String?,
  val prdno: String?,
  val grade: String?,
  val quant: Double?,
  val preco: Double?,
  val descricao: String?,
  val localizacao: String?,
  val cl: Int?,
  val estoque: Double?,
  val fornecedor: Int?,
  val tipo: Int?,
  val obs: String?,
) {
  val total
    get() = if (quant == null || preco == null) null else quant * preco
}