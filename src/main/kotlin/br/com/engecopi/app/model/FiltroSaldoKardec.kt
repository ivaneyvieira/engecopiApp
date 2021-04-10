package br.com.engecopi.app.model

import java.time.LocalDate

class FiltroSaldoKardec(
  var dataInicial: LocalDate? = LocalDate.now(), var dataFinal: LocalDate? = LocalDate.now()
                       )