package br.com.engecopi.app

import br.com.engecopi.app.forms.processaConferencia.ProcessaConferenciaForm
import br.com.engecopi.app.forms.processaPedido.PedidosMovForm
import br.com.engecopi.app.forms.saldoKardec.SaldoKardecForm
import com.github.vok.karibudsl.*
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme

@AutoView("")
class AppView : VerticalLayout(), View {

  init {
    setSizeFull()
    isMargin = false
    isSpacing = false
    tabSheet {
      setSizeFull()
      styleName = ValoTheme.TABSHEET_FRAMED
      addTab(PedidosMovForm(), "Movimentação de Pedidos")
      addTab(SaldoKardecForm(), "Compara Saldo vs Kardec")
      addTab(ProcessaConferenciaForm(), "Conferência")
      expandRatio = 1f
    }

    panel {
      addStyleName(ValoTheme.PANEL_BORDERLESS)
      verticalLayout {
        isMargin = false
        addStyleName(ValoTheme.LAYOUT_CARD)
        label("Servidor: " + br.com.engecopi.saci.QuerySaci.ipServer)
      }
    }


  }

  override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
  }
}