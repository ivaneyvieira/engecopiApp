package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.saci.beans.PedidoProduto
import com.github.vok.karibudsl.column
import com.github.vok.karibudsl.grid
import com.github.vok.karibudsl.panel
import com.github.vok.karibudsl.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme
import java.text.DecimalFormat

class GridPainel : CssLayout() {
  val grid = grid(PedidoProduto::class, null,
                  ListDataProvider<PedidoProduto>(emptyList())) {
    setSizeFull()

    column(PedidoProduto::prdno) {
      caption = "Código"
    }
    column(PedidoProduto::descricao) {
      caption = "Descricao"
    }
    column(PedidoProduto::grade) {
      caption = "Grade"
    }
    column(PedidoProduto::quant) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Quantidade"
    }

    column(PedidoProduto::preco) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Custo Contabil"
    }

    showColumns(PedidoProduto::prdno,PedidoProduto::descricao, PedidoProduto::grade,
                PedidoProduto::quant, PedidoProduto::preco)
  }


  init {
    caption = "Produtos"
    setSizeFull()
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      setSizeFull()
      content = grid
    }
  }
}