package br.com.engecopi.app.forms.ajustaEstoqueGarantia

import br.com.engecopi.app.model.Produtos
import com.vaadin.ui.VerticalLayout

class AjustaEstoqueGarantiaForm : VerticalLayout() {

  private val gridPanel = GridPanel(this)
  val headerPanel = HeaderPanel(this)

  init {
    setSizeFull()
    addComponents(headerPanel, gridPanel)
    addComponentsAndExpand(gridPanel)
  }

  fun setProdutos(produtos: List<Produtos>) {
    gridPanel.setProdutos(produtos)
  }
}