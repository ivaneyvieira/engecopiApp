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
    val loja = filtro.loja ?: fail("Loja não informada")
    val numPedido = filtro.numPedido ?: fail("Numero do pedido/nota não informado")
    val pedido = filtro.findPedido() ?: fail("Numero do pedido/nota não encontrado")
    val tipoNota = filtro.tipoNota ?: fail("O Tipo da nota não foi informada")
    val pedidoNotaTipo = pedido.tipo

    if (pedidoNotaTipo != PEDIDO) when {
      filtro.tipoMov != ENTRADA || pedidoNotaTipo != DEVOLUCAO -> fail("A nota não é de saida")
      filtro.tipoMov != SAIDA || pedidoNotaTipo != COMPRA -> fail("A nota não é de entrada")
    }
    val tipo = filtro.tipoMov ?: fail("Tipo do Movimento não foi informado")
    when {
      !pedido.isDataValida() -> {
        fail("Pedido tem mais de 366 dias")
      }

      !pedido.isLojaValida() -> {
        fail("O cliente da nota/pedidos não é ${loja.numero}")
      }
    }
    val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo, tipoNota)

    if (pedido.status != NAO_PROCESSADO && nota != null && nota.cancelado != true) fail("Nota já processada")
    else {
      if (pedido.isData30Dias()) {
        processa(pedido, tipo, tipoNota)
        //movimentacaoManualPainel.execFiltro(filtro)
      } else {
        messageConfirma("O pedido tem mais de 30 dias. Confirma?") {
          processa(pedido, tipo, tipoNota)
          // movimentacaoManualPainel.execFiltro(filtro)
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

  private fun processa(pedidoNota: PedidoNota, tipo: TipoMov, tipoNota: TipoNota) {
    val numPedido = pedidoNota.numero ?: ""
    val storeno = pedidoNota.storeno ?: 0
    when (tipoNota) {
      GARANTIA -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.processaNota(storeno, nfno, nfse, NF)
        } else saci.processaPedido(storeno, numPedido, tipo, tipoNota, NF)
      }

      PERDA -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.processaNota(storeno, nfno, nfse, STKMOV)
        } else saci.processaPedido(storeno, numPedido, tipo, tipoNota, STKMOV)
      }
    }
  }

  private fun desfaz(pedidoNota: PedidoNota, tipoMov: TipoMov, tipoNota: TipoNota) {
    val numPedido = pedidoNota.numeroPedido ?: ""
    val storeno = pedidoNota.storeno ?: 0
    when (tipoNota) {
      GARANTIA -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.desfazNota(storeno, nfno, nfse, NF)
        } else saci.desfazPedido(storeno, numPedido, tipoMov, NF)
      }

      PERDA -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.desfazNota(storeno, nfno, nfse, STKMOV)
        } else saci.desfazPedido(storeno, numPedido, tipoMov, STKMOV)
      }
    }
  }
}