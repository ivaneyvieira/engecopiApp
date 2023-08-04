package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.saci.saci
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
    val tipoMov = movimentacaoManualPainel.filtroBean().tipoMov
    val tipoNota = movimentacaoManualPainel.filtroBean().tipoNota

    if (selecionado.isEmpty()) fail("Nenhum item selecionado")
    if (tipoMov == null) fail("Tipo de movimentação não informado")
    if (tipoNota == null) fail("Tipo de nota não informado")

    when (tipoNota) {
      TipoNota.GARANTIA -> fail("Tipo de nota não implementado")
      TipoNota.PERDA -> {
        val verbo = when (tipoMov) {
          TipoMov.ENTRADA -> "ENTRADA"
          TipoMov.SAIDA -> "SAÌDA"
        }
        messageConfirma("Confirma a $verbo dos itens selecionados?") {
          val transacao = saci.executarMov(tipoMov, selecionado)
          movimentacaoManualPainel.setTransacao(transacao)
          gridPainel.updateSelection()
        }
      }
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