package br.com.engecopi.app.forms.processaConferencia

import com.vaadin.ui.VerticalLayout

class ProcessaConferenciaForm : VerticalLayout() {
  val gridPanel = GridPanel(this)
  val headerPanel = HeaderPanel(this)

  init {
    setSizeFull()
    addComponents(headerPanel, gridPanel)
    addComponentsAndExpand(gridPanel)
  }
}