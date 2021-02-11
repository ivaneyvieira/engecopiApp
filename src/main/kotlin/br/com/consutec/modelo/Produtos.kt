package br.com.consutec.modelo

class Produtos(val prdno: String, val grade: String, val descricao: String, val fornecedor: Long,
               val centrodelucro: String, val tipo: Long, val qtdAtacado: Int, val qtdNfForn: Int,
               val qtdConsiderada: Int, val custo: Double, val total: Double)