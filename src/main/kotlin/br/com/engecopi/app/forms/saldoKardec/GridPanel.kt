package br.com.engecopi.app.forms.saldoKardec

import br.com.engecopi.saci.beans.SaldoKardec
import com.github.mvysny.karibudsl.v8.column
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme
import java.text.DecimalFormat

class GridPanel : CssLayout() {
  val grid = grid(SaldoKardec::class, null,
                  ListDataProvider(emptyList())) {
    setSizeFull()

    column(SaldoKardec::codigo) {
      caption = "Código"
    }

    column(SaldoKardec::grade) {
      caption = "Grade"
    }

    column(SaldoKardec::loja) {
      caption = "Loja"
    }

    column(SaldoKardec::mes_ano) {
      caption = "Mes/Ano"
    }

    column(SaldoKardec::saldoEstoque) {
      caption = "Estoque"
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
    }

    column(SaldoKardec::saldoKardec) {
      caption = "Kardec"
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
    }

    column(SaldoKardec::diferecenca) {
      caption = "Diferença"
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
    }

    showColumns(SaldoKardec::codigo, SaldoKardec::grade, SaldoKardec::loja,
                SaldoKardec::mes_ano, SaldoKardec::saldoEstoque, SaldoKardec::saldoKardec,
                SaldoKardec::diferecenca)
  }

  init {
    caption = "Resultado"
    setSizeFull()
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      setSizeFull()
      content = grid
    }
  }

  fun setItens(itens: List<SaldoKardec>) {
    grid.dataProvider = ListDataProvider(itens)
  }
}