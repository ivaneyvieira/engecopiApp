package br.com.engecopi.app.model

data class FiltroMov(
  var tipoMov: TipoMov?,
  var tipoNota: TipoNota?,
  var loja: Loja?,
  var transacao: String,
)
