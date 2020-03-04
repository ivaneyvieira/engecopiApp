package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.saci.beans.Pedido
import br.com.engecopi.saci.saci
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout

class PedidosMovForm : VerticalLayout() {
  val filtroPedidoPainel = FiltroPedidoPainel()
  val pedidoPainel = PedidoPainel()
  val gridPainel = GridPainel()

  init {
    filtroPedidoPainel.execFiltro = { filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)
      val tipo = filtro.tipoMov?.cod
      if (pedido == null)
        Notification.show("Pedido não encontrado", Notification.Type.WARNING_MESSAGE)

      pedidoPainel.setPedido(pedido, tipo)
      val produtos = saci.pedidoProduto(loja, numPedido)
      gridPainel.grid.dataProvider = ListDataProvider(produtos)
    }

    filtroPedidoPainel.execProcessa = { filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val tipo = filtro.tipoMov?.cod ?: ""
      val tipoNota = filtro.tipoNota
      val pedido = saci.pedidoNota(loja, numPedido)
      val nota = saci.pesquisaNota(loja, numPedido, tipo)
      when {
        pedido == null     -> {
          Notification.show("Esse pedido não foi encontrado", Notification.Type.WARNING_MESSAGE)
        }
        pedido.status == 1 -> {
          saci.processaPedido(loja, numPedido, tipo, tipoNota)
          filtroPedidoPainel.execFiltro(filtro)
        }
        else               -> {
          when {
            nota == null           -> {
              saci.processaPedido(loja, numPedido, tipo, tipoNota)
              filtroPedidoPainel.execFiltro(filtro)
            }
            nota.cancelado == true -> {
              saci.processaPedido(loja, numPedido, tipo, tipoNota)
              filtroPedidoPainel.execFiltro(filtro)
            }
            else                   -> {
              Notification.show("Nota já processada", Notification.Type.WARNING_MESSAGE)
            }
          }
        }
      }
    }

    filtroPedidoPainel.desfazProcessa = { filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val tipo = filtro.tipoMov?.cod ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)
      val nota = saci.pesquisaNota(loja, numPedido, tipo)
      when (pedido) {
        null -> {
          Notification.show("Esse pedido não foi encontrado", Notification.Type.WARNING_MESSAGE)
        }
        else -> {
          when {
            nota == null           -> {
              Notification.show("Esse pedido não foi processado", Notification.Type.WARNING_MESSAGE)
            }
            nota.cancelado == true -> {
              Notification.show("Esse pedido não foi processado", Notification.Type.WARNING_MESSAGE)
            }
            else                   -> {
              saci.desfazPedido(loja, numPedido, tipo)
            }
          }
          filtroPedidoPainel.execFiltro(filtro)
        }
      }
    }

    setSizeFull()
    addComponents(filtroPedidoPainel, pedidoPainel)
    addComponentsAndExpand(gridPainel)
  }

  private fun notaHabilitada(pedido: Pedido, tipo: String?): Boolean {
    val notaFiscal = pedido.notaFiscal(tipo ?: "")
    val habilitada = notaFiscal?.let { it.cancelado == false } ?: false
    return habilitada
  }
}