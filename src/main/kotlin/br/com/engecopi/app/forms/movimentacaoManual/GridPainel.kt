package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.Produtos
import br.com.engecopi.utils.format
import com.github.mvysny.karibudsl.v8.column
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.components.grid.FooterCell
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme.LAYOUT_WELL
import java.text.DecimalFormat

class GridPainel : CssLayout() {
  private var totalFotter: FooterCell? = null
  val grid = grid(Produtos::class, null, ListDataProvider(emptyList())) {
    setSizeFull()

    column(Produtos::prdno) {
      caption = "Código"
      expandRatio = 1
    }
    column(Produtos::descricao) {
      caption = "Descricao"
      expandRatio = 3
    }
    column(Produtos::grade) {
      caption = "Grade"
      expandRatio = 1
    }
    column(Produtos::fornecedor) {
      caption = "Fornecedor"
      expandRatio = 1
    }
    column(Produtos::centrodelucro) {
      caption = "CL"
      expandRatio = 1
    }
    column(Produtos::tipo) {
      caption = "Tipo"
      expandRatio = 1
    }
    column(Produtos::qtdAtacado) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Estoque"
      expandRatio = 1
    }
    column(Produtos::custo) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Custo Real"
      expandRatio = 1
    }

    showColumns(
      Produtos::prdno,
      Produtos::descricao,
      Produtos::grade,
      Produtos::fornecedor,
      Produtos::centrodelucro,
      Produtos::tipo,
      Produtos::qtdAtacado,
      Produtos::custo,
    )
  }

  fun setItens(itens: List<Produtos>) {
    grid.dataProvider = ListDataProvider(itens)
    val total = itens.sumByDouble { it.total ?: 0.0 }
    totalFotter?.html = "<font size=\"4\">${total.format()}</font>"
  }

  init {
    caption = "Produtos"
    setSizeFull()
    styleName = LAYOUT_WELL
    panel {
      setSizeFull()
      content = grid
    }
  }
}