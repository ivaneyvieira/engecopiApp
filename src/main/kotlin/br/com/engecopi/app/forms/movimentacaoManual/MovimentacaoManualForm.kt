package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.FiltroMov
import com.vaadin.ui.Notification.Type.ERROR_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.VerticalLayout
import de.steinwedel.messagebox.ButtonOption
import de.steinwedel.messagebox.MessageBox

class MovimentacaoManualForm : VerticalLayout() {
  private val movimentacaoManualPainel = MovimentacaoManualPainel(::execProcessa)
  private val gridPainel = GridPainel()
  private val filtroPainel = FiltroPainel(movimentacaoManualPainel, gridPainel)

  init {
    setSizeFull()
    addComponents(movimentacaoManualPainel, filtroPainel)
    addComponentsAndExpand(gridPainel)
  }

  fun fail(msg: String): Nothing {
    show(msg, ERROR_MESSAGE)
    throw Exception(msg)
  }

  private fun execProcessa() {
    gridPainel.execProcessa()
    val selecionado = gridPainel.itensSelecionado()
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