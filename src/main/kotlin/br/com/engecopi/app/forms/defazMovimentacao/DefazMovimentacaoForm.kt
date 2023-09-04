package br.com.engecopi.app.forms.defazMovimentacao

import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.saci.saci
import com.vaadin.ui.Notification.Type.ERROR_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.VerticalLayout
import de.steinwedel.messagebox.ButtonOption
import de.steinwedel.messagebox.MessageBox

class DefazMovimentacaoForm : VerticalLayout() {
  private val defazMovimentacaoPainel = DefazMovimentacaoPainel(execBusca = ::execBusca, execProcessa = ::execProcessa)
  private val gridPainel = GridPainel()

  init {
    setSizeFull()
    addComponents(defazMovimentacaoPainel)
    addComponentsAndExpand(gridPainel)
  }

  private fun execProcessa() {
    val filtro = defazMovimentacaoPainel.filtroTransacao()
    gridPainel.itensSelecionado().let { produtos ->
      saci.removeProdutos(filtro, produtos)
    }
    execBusca()
  }

  private fun execBusca() {
    saci.buscaProdutosMovimentacaoManual(defazMovimentacaoPainel.filtroTransacao()).let { produtos ->
      gridPainel.addItens(produtos)
    }
  }

  private fun messageConfirma(msgConfirmacao: String, executeProcesso: () -> Unit) {
    MessageBox
      .create()
      .withCaption("Confirmação")
      .withMessage(msgConfirmacao)
      .withYesButton({ executeProcesso() }, ButtonOption.caption("Sim"))
      .withNoButton({ println("No button was pressed.") }, ButtonOption.caption("Não"))
      .open()
  }
}