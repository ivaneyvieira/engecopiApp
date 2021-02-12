package br.com.engecopi.app.model

import java.math.BigDecimal

class Base {
  var lojaDestino: Int? = null
  var operacao: String? = null
  var codprd: String? = null
  var fornecedores: String? = null
  var tipos: String? = null
  var incluiNfFornecedor: Boolean? = null
  var fornecedorNf: String? = null

  var descIni: String? = ""
  var descFim: String? = ""
  var lojaArea: Int? = 0
  var areas: String? = ""
  var centrodeLucro: String? = ""
  var qtd1: BigDecimal? = BigDecimal.ZERO
  var qtd2: BigDecimal? = BigDecimal.ZERO
  var custo1: BigDecimal? = BigDecimal.ZERO
  var custo2: BigDecimal? = BigDecimal.ZERO
  var sinalQtd: String? = "todos"
  var sinalCusto: String? = "todos"
  var codFuncionario: Int? = 0
  var usaFiltrosProduto: Boolean? = true
  var tipoCusto: String? = "est"
  var dtNfIni: Int? = 0
  var dtNfFim: Int? = 0
}