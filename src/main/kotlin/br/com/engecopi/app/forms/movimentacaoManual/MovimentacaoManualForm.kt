package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.app.model.TipoMov.SAIDA
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.app.model.TipoNota.GARANTIA
import br.com.engecopi.app.model.TipoNota.PERDA
import br.com.engecopi.saci.DestinoMov.NF
import br.com.engecopi.saci.DestinoMov.STKMOV
import br.com.engecopi.saci.beans.PedidoNota
import br.com.engecopi.saci.beans.StatusPedido.NAO_PROCESSADO
import br.com.engecopi.saci.beans.TipoPedido.*
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

  private fun execProcessa(filtro: FiltroPedido) {

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

  private fun processa(pedidoNota: PedidoNota, tipo: TipoMov, tipoNota: TipoNota) {

  }
}