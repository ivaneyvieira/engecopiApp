package br.com.engecopi.app.forms.movimentacaoPedido

import br.com.engecopi.saci.beans.PedidoProduto
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
  val grid = grid(PedidoProduto::class, null, ListDataProvider(emptyList())) {
    setSizeFull()

    column(PedidoProduto::prdno) {
      caption = "Código"
      expandRatio = 1
    }
    column(PedidoProduto::descricao) {
      caption = "Descricao"
      expandRatio = 3
    }
    column(PedidoProduto::grade) {
      caption = "Grade"
      expandRatio = 1
    }
    column(PedidoProduto::localizacao) {
      caption = "Localização"
      this.setWidth(120.0)
    }
    column(PedidoProduto::fornecedor) {
      caption = "Fornecedor"
      expandRatio = 1
    }
    column(PedidoProduto::cl) {
      caption = "CL"
      expandRatio = 1
    }
    column(PedidoProduto::tipo) {
      caption = "Tipo"
      expandRatio = 1
    }
    column(PedidoProduto::estoque) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Estoque"
      expandRatio = 1
    }
    column(PedidoProduto::quant) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Quantidade"
      expandRatio = 1
    }
    column(PedidoProduto::preco) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Custo Real"
      expandRatio = 1
    }
    column(PedidoProduto::total) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Total"
      expandRatio = 1
    }
    column(PedidoProduto::obs) {
      caption = "Obs"
      expandRatio = 1
    }

    showColumns(
      PedidoProduto::prdno,
      PedidoProduto::descricao,
      PedidoProduto::grade,
      PedidoProduto::quant,
      PedidoProduto::estoque,
      PedidoProduto::obs,
      PedidoProduto::localizacao,
      PedidoProduto::fornecedor,
      PedidoProduto::cl,
      PedidoProduto::tipo,
      PedidoProduto::preco,
      PedidoProduto::total,
    )

    val fotter = this.appendFooterRow()

    fotter.getCell(PedidoProduto::preco.name).apply {
      this.html = "<font size=\"4\"><b>Total:</b>"
      this.styleName = "v-align-right"
    }

    totalFotter = fotter.getCell(PedidoProduto::total.name).apply {
      this.html = ""
      this.styleName = "v-align-right"
    }
  }

  fun setItens(itens: List<PedidoProduto>) {
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