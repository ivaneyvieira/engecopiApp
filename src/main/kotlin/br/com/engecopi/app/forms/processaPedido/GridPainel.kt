package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.saci.beans.PedidoProduto
import com.github.mvysny.karibudsl.v8.column
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.showColumns
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
      expandRatio = 5
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
      caption = "Custo Contabil"
      expandRatio = 1
    }

    showColumns(PedidoProduto::prdno, PedidoProduto::descricao, PedidoProduto::grade,
                PedidoProduto::localizacao, PedidoProduto::quant, PedidoProduto::preco)
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