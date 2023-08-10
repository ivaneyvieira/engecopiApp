package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.ProdutosMovManual
import br.com.engecopi.utils.format
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.CssLayout
import com.vaadin.ui.Grid.SelectionMode
import com.vaadin.ui.components.grid.FooterCell
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme.LAYOUT_WELL
import org.vaadin.viritin.fields.IntegerField
import java.text.DecimalFormat


class GridPainel : CssLayout() {
  private var totalFotter: FooterCell? = null
  val grid = grid(ProdutosMovManual::class, null, ListDataProvider(emptyList())) {
    setSizeFull()
    this.setSelectionMode(SelectionMode.MULTI)

    val edtQuantidade = IntegerField().apply {
      addStyleName("align-right")
      addStyleName(LAYOUT_WELL)
      setWidth("100%")
    }

    val binder = this.getEditor().getBinder()

    //Coluna sequancial
    column(ProdutosMovManual::sequencial) {
      caption = "Seq"
      expandRatio = 1
    }
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
      caption = "Estoque"
      expandRatio = 1
    }
    column(ProdutosMovManual::saldoTotal) {
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Estoque Total"
      expandRatio = 1
    }
    column(ProdutosMovManual::obs) {
      caption = "Obs"
      expandRatio = 2
    }
    column(ProdutosMovManual::loc) {
      caption = "Loc MF"
      expandRatio = 1
    }
    column(ProdutosMovManual::qtty) {
      setEditorComponent(edtQuantidade) { bean, valor ->
        bean.qtty = valor
      }
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Quant Ajuste"
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
      ProdutosMovManual::sequencial,
      ProdutosMovManual::prdno,
      ProdutosMovManual::descricao,
      ProdutosMovManual::grade,
      ProdutosMovManual::obs,
      ProdutosMovManual::loc,
      ProdutosMovManual::qtty,
      ProdutosMovManual::saldo,
      ProdutosMovManual::saldoTotal,
      ProdutosMovManual::fornecedor,
      ProdutosMovManual::centrodelucro,
      ProdutosMovManual::tipo,
      ProdutosMovManual::custo,
      ProdutosMovManual::total,
    )

    this.editor.saveCaption = "Salvar"
    this.editor.cancelCaption = "Cancelar"
    this.editor.isEnabled = true

    this.editor.addSaveListener {
      updateTotal()
    }

    val fotter = this.appendFooterRow()

    fotter.getCell(ProdutosMovManual::total.name).apply {
      this.html = "<font size=\"4\"><b>Total:</b>"
      this.styleName = "v-align-right"
    }

    totalFotter = fotter.getCell(ProdutosMovManual::total.name).apply {
      this.html = ""
      this.styleName = "v-align-right"
    }

    this.addSelectionListener {
      updateTotal()
    }


    this.dataProvider.addDataProviderListener {
      updateTotal()
    }
  }

  fun addItens(itens: List<ProdutosMovManual>) {
    val selectedItems = grid.selectedItems.onEach { it.updateSaldo() }.sortedBy { it.prdno }
    val resto = (itens - selectedItems.toSet()).sortedBy { it.prdno }
    val novosItens = selectedItems + resto

    grid.dataProvider = ListDataProvider(novosItens)
    selectedItems.forEach {
      grid.selectionModel.select(it)
    }

    updateTotal()
  }

  private fun updateTotal() {
    val itens = grid.dataProvider.getAll()
    val total = itens.sumByDouble { it.total }
    totalFotter?.html = "<font size=\"4\">${total.format()}</font>"

    itens.forEachIndexed { index, produtosMovManual ->
      produtosMovManual.sequencial = index + 1
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