package br.com.engecopi.app.model

class Produtos(
  var prdno: String,
  var grade: String,
  var descricao: String,
  var fornecedor: Long,
  var centrodelucro: String,
  var tipo: Long,
  var qtdAtacado: Double,
  var qtdNfForn: Double,
  var qtdConsiderada: Double,
  var custo: Double,
  var total: Double,
)