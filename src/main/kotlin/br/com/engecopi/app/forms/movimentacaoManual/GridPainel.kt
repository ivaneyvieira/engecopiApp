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
import org.vaadin.viritin.fields.IntegerField
import java.text.DecimalFormat


class GridPainel : CssLayout() {
  val grid = grid(ProdutosMovManual::class, null, ListDataProvider(emptyList())) {
    setSizeFull()
    this.setSelectionMode(SelectionMode.MULTI)

    val edtQuantidade = IntegerField().apply {
      addStyleName("align-right")
      addStyleName(LAYOUT_WELL)
      setWidth("100%")
    }

    val binder = this.getEditor().getBinder()

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
    column(ProdutosMovManual::saldo) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Saldo"
      expandRatio = 1
    }
    column(ProdutosMovManual::qtty) {
      setEditorComponent(edtQuantidade) { bean, valor ->
        bean.qtty = valor
      }
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
      ProdutosMovManual::saldo,
      ProdutosMovManual::qtty,
      ProdutosMovManual::custo,
      ProdutosMovManual::total,
    )

    this.editor.saveCaption = "Salvar"
    this.editor.cancelCaption = "Cancelar"
    this.editor.isEnabled = true
  }

  fun addItens(itens: List<ProdutosMovManual>) {
    val selectedItems = grid.selectedItems.onEach { it.updateSaldo() }.sortedBy { it.prdno }
    val resto = (itens - selectedItems.toSet()).sortedBy { it.prdno }
    val novosItens = selectedItems + resto

    grid.dataProvider = ListDataProvider(novosItens)
    selectedItems.forEach {
      grid.selectionModel.select(it)
    }
  }

  fun itensSelecionado(): List<ProdutosMovManual> {
    return grid.selectedItems.toList()
  }

  fun updateSelection() {
    val selecionado = grid.selectedItems.toList()
    selecionado.forEach { it.updateSaldo() }
    grid.setItems(selecionado)
  }

  fun limpaProdutos() {
    grid.dataProvider = ListDataProvider(emptyList())
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