package br.com.engecopi.app.forms.saldoKardec

import br.com.engecopi.saci.beans.PedidoProduto
import br.com.engecopi.saci.beans.SaldoKardec
import com.github.vok.karibudsl.column
import com.github.vok.karibudsl.grid
import com.github.vok.karibudsl.panel
import com.github.vok.karibudsl.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme
import java.text.DecimalFormat

class GridPanel : CssLayout() {
  val grid = grid(SaldoKardec::class, null,
                  ListDataProvider<SaldoKardec>(emptyList())) {
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
    grid.dataProvider = ListDataProvider<SaldoKardec>(itens)
  }
}