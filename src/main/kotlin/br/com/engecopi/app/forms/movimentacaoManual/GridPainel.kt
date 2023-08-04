package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.ProdutosMovManual
import com.github.mvysny.karibudsl.v8.column
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.Grid.SelectionMode
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme.LAYOUT_WELL
import java.text.DecimalFormat

class GridPainel : CssLayout() {
  private val selecionado = mutableSetOf<ProdutosMovManual>()
  val grid = grid(ProdutosMovManual::class, null, ListDataProvider(emptyList())) {
    setSizeFull()
    this.setSelectionMode(SelectionMode.MULTI)

    column(ProdutosMovManual::prdno) {
      caption = "Código"
      expandRatio = 1
    }
    column(ProdutosMovManual::descricao) {
      caption = "Descricao"
      expandRatio = 3
    }
    column(ProdutosMovManual::grade) {
      caption = "Grade"
      expandRatio = 1
    }
    column(ProdutosMovManual::fornecedor) {
      caption = "Fornecedor"
      expandRatio = 1
    }
    column(ProdutosMovManual::centrodelucro) {
      caption = "CL"
      expandRatio = 1
    }
    column(ProdutosMovManual::tipo) {
      caption = "Tipo"
      expandRatio = 1
    }
    column(ProdutosMovManual::qtty) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Quantidade"
      expandRatio = 1
    }
    column(ProdutosMovManual::custo) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Custo Real"
      expandRatio = 1
    }
    column(ProdutosMovManual::total) {
      setRenderer(NumberRenderer(DecimalFormat("0.00")))
      setStyleGenerator { "v-align-right" }
      caption = "Total"
      expandRatio = 1
    }

    showColumns(
      ProdutosMovManual::prdno,
      ProdutosMovManual::descricao,
      ProdutosMovManual::grade,
      ProdutosMovManual::fornecedor,
      ProdutosMovManual::centrodelucro,
      ProdutosMovManual::tipo,
      ProdutosMovManual::qtty,
      ProdutosMovManual::custo,
      ProdutosMovManual::total,
    )
  }

  fun setItens(itens: List<ProdutosMovManual>) {
    val selectedItems = grid.selectedItems
    selecionado.addAll(selectedItems)
    val novosItens = mutableListOf<ProdutosMovManual>()
    novosItens.addAll(itens)
    novosItens.addAll(selecionado)
    grid.dataProvider = ListDataProvider(novosItens)
    selecionado.forEach {
      grid.selectionModel.select(it)
    }
  }

  fun execProcessa() {
    val itens = grid.selectedItems
    selecionado.addAll(itens)
    grid.dataProvider = ListDataProvider(selecionado)
    selecionado.forEach {
      grid.selectionModel.select(it)
    }
  }

  fun itensSelecionado() : List<ProdutosMovManual> {
    return grid.selectedItems.toList()
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