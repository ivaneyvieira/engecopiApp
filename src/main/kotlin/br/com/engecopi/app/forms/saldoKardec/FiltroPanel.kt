package br.com.engecopi.app.forms.saldoKardec

import br.com.engecopi.app.model.FiltroSaldoKardec
import com.github.vok.karibudsl.beanValidationBinder
import com.github.vok.karibudsl.bind
import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.dateField
import com.github.vok.karibudsl.horizontalLayout
import com.github.vok.karibudsl.isMargin
import com.github.vok.karibudsl.panel
import com.vaadin.ui.Alignment
import com.vaadin.ui.CssLayout
import com.vaadin.ui.themes.ValoTheme

class FiltroPanel : CssLayout() {
  private val binder = beanValidationBinder<FiltroSaldoKardec>()
  lateinit var execExcel: (FiltroSaldoKardec) -> Unit
  lateinit var execFiltro: (FiltroSaldoKardec) -> Unit
  val dataInicial = dateField("Data Inicial") {
    bind(binder).bind(FiltroSaldoKardec::dataInicial)
    dateFormat = "dd/MM/yyyy"
  }
  val dataFinal = dateField("Data Final") {
    bind(binder).bind(FiltroSaldoKardec::dataFinal)
    dateFormat = "dd/MM/yyyy"
  }
  
  val filtro: FiltroSaldoKardec? = FiltroSaldoKardec()
  
  val btnExcel = button("Gera Planilha", {
    addClickListener {
      if (binder.writeBeanIfValid(filtro)) {
        filtro?.let {
          execExcel(it)
        }
      }
    }
  })
  
  val btnProcessa = button("Processamento", {
    addClickListener {
      if (binder.writeBeanIfValid(filtro)) {
        filtro?.let {
          execFiltro(it)
        }
      }
    }
  })
  
  init {
    caption = "Filtro"
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      horizontalLayout {
        setWidth("100%")
        isMargin = true
        addComponents(dataInicial, dataFinal, btnExcel, btnProcessa)
        setComponentAlignment(dataInicial, Alignment.BOTTOM_LEFT)
        setComponentAlignment(dataFinal, Alignment.BOTTOM_LEFT)
        setComponentAlignment(btnExcel, Alignment.BOTTOM_RIGHT)
        setComponentAlignment(btnProcessa, Alignment.BOTTOM_RIGHT)
        setExpandRatio(btnExcel, 1f)
      }
    }
    binder.readBean(filtro)
  }
}