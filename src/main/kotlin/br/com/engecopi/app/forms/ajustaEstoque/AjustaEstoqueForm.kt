package br.com.engecopi.app.forms.ajustaEstoque

import br.com.engecopi.app.model.Produtos
import com.vaadin.ui.VerticalLayout

class AjustaEstoqueForm : VerticalLayout() {
  
  val gridPanel = GridPanel(this)
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