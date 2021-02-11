package br.com.consutec.modelo

class Base(val lojaDestino: Int, val operacao: String, val codprd: String, val descIni: String = "",
           val descFim: String = "", val fornecedores: String, val tipos: String, val lojaArea: Int = 0,
           val areas: String = "", val centrodeLucro: String = "", val qtd1: Int = Int.MIN_VALUE,
           val qtd2: Int = Int.MAX_VALUE, val custo1: Double = Double.MIN_VALUE, val custo2: Double = Double.MAX_VALUE,
           val sinalQtd: String = "todos", val sinalCusto: String = "todos", val usaFiltrosProduto: Boolean = false,
           val tipoCusto: String = "est", val incluiNfFornecedor: Boolean, val fornecedorNf: String, val dtNfIni: Int = 0,
           val dtNfFim: Int = 0)