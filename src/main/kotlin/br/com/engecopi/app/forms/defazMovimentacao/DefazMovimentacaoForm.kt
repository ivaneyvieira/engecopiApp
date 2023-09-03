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
  private val defazMovimentacaoPainel = DefazMovimentacaoPainel(::execProcessa, ::execDesfaz)
  private val gridPainel = GridPainel()
  private val filtroPainel = FiltroPainel(defazMovimentacaoPainel, gridPainel)

  init {
    setSizeFull()
    addComponents(defazMovimentacaoPainel, filtroPainel)
    addComponentsAndExpand(gridPainel)
  }

  fun fail(msg: String): Nothing {
    show(msg, ERROR_MESSAGE)
    throw Exception(msg)
  }

  private fun execProcessa() {
    val selecionado = gridPainel.itensSelecionado()
    val tipoMov = defazMovimentacaoPainel.filtroBean().tipoMov
    val tipoNota = defazMovimentacaoPainel.filtroBean().tipoNota

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
          defazMovimentacaoPainel.setTransacao(transacao)
          gridPainel.updateSelection()
        }
      }
    }
  }

  private fun execDesfaz() {
    val filtroBean = defazMovimentacaoPainel.filtroBean()
    val tipoMov = filtroBean.tipoMov
    val tipoNota = filtroBean.tipoNota
    val transacao = filtroBean.transacao
    val loja = filtroBean.loja

    if (transacao.isEmpty()) fail("Nenhuma transação selecionada")
    if (tipoMov == null) fail("Tipo de movimentação não informado")
    if (tipoNota == null) fail("Tipo de nota não informado")
    if (loja == null) fail("Loja não informada")

    when (tipoNota) {
      TipoNota.GARANTIA -> fail("Tipo de nota não implementado")
      TipoNota.PERDA -> {
        val verbo = when (tipoMov) {
          TipoMov.ENTRADA -> "ENTRADA"
          TipoMov.SAIDA -> "SAÌDA"
        }
        messageConfirma("Desfaz a $verbo dos itens selecionados?") {
          saci.desfazInventarioMov(tipoMov, transacao, loja)
          defazMovimentacaoPainel.setTransacao("")
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