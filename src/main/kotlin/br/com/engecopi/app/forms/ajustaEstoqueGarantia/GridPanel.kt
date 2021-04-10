package br.com.engecopi.app.forms.ajustaEstoqueGarantia

import br.com.engecopi.app.model.Produtos
import br.com.engecopi.utils.format
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme
import java.text.DecimalFormat

class GridPanel(val ajustaEstoqueFormPerda: AjustaEstoqueFormGarantia) : CssLayout() {

  val grid = grid(Produtos::class, null, ListDataProvider(emptyList())) {
    setSizeFull()

    column(Produtos::prdno) {
      caption = "Código Prod"
    }

    column(Produtos::grade) {
      caption = "Grade"
    }

    column(Produtos::descricao) {
      caption = "Descrição"
    }

    column(Produtos::fornecedor) {
      caption = "Fornecedor"
    }

    column(Produtos::centrodelucro) {
      caption = "Centro de Lucro"
    }

    column(Produtos::tipo) {
      caption = "Tipo"
    }

    column(Produtos::qtdAtacado) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Qtde Atacado"
    }

    column(Produtos::custo) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Custo"
    }

    column(Produtos::total) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Total"
    }

    val footer = prependFooterRow()

    this.addSelectionListener {
      val total = dataProvider.getAll().sumByDouble { it.total }
      footer.getCell(Produtos::total).text = total.format()
    }

    showColumns(
      Produtos::prdno,
      Produtos::grade,
      Produtos::descricao,
      Produtos::fornecedor,
      Produtos::centrodelucro,
      Produtos::tipo,
      Produtos::qtdAtacado,
      Produtos::custo,
      Produtos::total
               )
  }

  fun setProdutos(produtos: List<Produtos>) {
    grid.setItems(produtos)
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