package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.saci.beans.Pedido
import br.com.engecopi.saci.saci
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Notification.Type.WARNING_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.VerticalLayout

class PedidosMovForm : VerticalLayout() {
  private val filtroPedidoPainel = FiltroPedidoPainel()
  private val pedidoPainel = PedidoPainel()
  private val gridPainel = GridPainel()

  init {
    filtroPedidoPainel.execFiltro = { filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)

      pedido?.let { ped ->
        if (ped.tipo == DEVOLUCAO) {
          filtro.tipoMov = ENTRADA
          filtro.tipoNota = 7
        }
      }
      val tipoNota = filtro.tipoNota
      val tipo = filtro.tipoMov?.cod ?: ""
      val pedidoValido = validaPedido(pedido)
      pedidoPainel.setPedido(pedidoValido, tipo)
      when {
        pedidoValido == null                        -> {
          show("Esse pedido não foi encontrado", WARNING_MESSAGE)
        }

        tipoNota == 7 && pedidoValido.isEngecopi()  -> {
          show("O cliente não pode ser loja", WARNING_MESSAGE)
        }

        tipoNota == 9 && !pedidoValido.isEngecopi() -> {
          show("O cliente deve ser uma loja", WARNING_MESSAGE)
        }

        !pedidoValido.produtoValido()               -> {
          show("O pedido possui um produto com código maior que 980000", WARNING_MESSAGE)
        }
      }
      setProdutosGrid(pedidoValido)
    }

    filtroPedidoPainel.execProcessa = { filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)

      pedido?.let { ped ->
        if (ped.tipo == DEVOLUCAO) {
          filtro.tipoMov = ENTRADA
          filtro.tipoNota = 7
        }
      }
      val tipo = filtro.tipoMov?.cod ?: ""
      val tipoNota = filtro.tipoNota
      val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo)
      val pedidoValido = validaPedido(pedido)

      when {
        pedidoValido == null                        -> {
          show("Esse pedido não foi encontrado", WARNING_MESSAGE)
        }

        tipoNota == 7 && pedidoValido.isEngecopi()  -> {
          show("O cliente não pode ser loja", WARNING_MESSAGE)
        }

        tipoNota == 9 && !pedidoValido.isEngecopi() -> {
          show("O cliente deve ser uma loja", WARNING_MESSAGE)
        }

        pedidoValido.status == 1                    -> {
          processa(pedido, loja, numPedido, tipo, tipoNota)
          filtroPedidoPainel.execFiltro(filtro)
        }

        else                                        -> {
          when {
            nota == null           -> {
              processa(pedido, loja, numPedido, tipo, tipoNota)
              filtroPedidoPainel.execFiltro(filtro)
            }

            nota.cancelado == true -> {
              processa(pedido, loja, numPedido, tipo, tipoNota)
              filtroPedidoPainel.execFiltro(filtro)
            }

            else                   -> {
              show("Nota já processada", WARNING_MESSAGE)
            }
          }
        }
      }
    }

    filtroPedidoPainel.desfazProcessa = { filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val tipo = filtro.tipoMov?.cod ?: ""
      val tipoNota = filtro.tipoNota
      val pedido = saci.pedidoNota(loja, numPedido)
      val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo)
      when (pedido) {
        null -> {
          show("Esse pedido não foi encontrado", WARNING_MESSAGE)
        }

        else -> {
          when {
            nota == null           -> {
              show("Esse pedido não foi processado", WARNING_MESSAGE)
            }

            nota.cancelado == true -> {
              show("Esse pedido não foi processado", WARNING_MESSAGE)
            }

            else                   -> {
              desfaz(pedido, loja, numPedido, tipo, tipoNota)
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

  private fun validaPedido(pedido: Pedido?): Pedido? {
    val lojaNome = pedido?.loja?.descricao
    return when {
      pedido == null         -> {
        show("Pedido não encontrado", WARNING_MESSAGE)
        null
      }

      !pedido.isDataValida() -> {
        show("Pedido tem mais de 30 dias", WARNING_MESSAGE)
        null
      }

      !pedido.isLojaValida() -> {
        show("O cliente da nota/pedidos não é $lojaNome", WARNING_MESSAGE)
        null
      }

      else                   -> pedido
    }
  }

  private fun setProdutosGrid(pedido: Pedido?) {
    val produtos = pedido?.produtos().orEmpty()
    gridPainel.grid.dataProvider = ListDataProvider(produtos)
  }

  private fun processa(pedido: Pedido?, loja: Int, numPedido: String, tipo: String, tipoNota: Int) {
    if (pedido?.tipo == DEVOLUCAO) {
      val nfno = pedido.numeroPedido ?: ""
      val nfse = pedido.serie ?: ""
      saci.processaDevolucaoSTKMOV(loja, nfno, nfse, tipoNota)
    }
    else saci.processaPedidoSTKMOV(loja, numPedido, tipo, tipoNota)
  }

  private fun desfaz(pedido: Pedido?, loja: Int, numPedido: String, tipo: String, tipoNota: Int) {
    if (pedido?.tipo == DEVOLUCAO) {
      val nfno = pedido.numeroPedido ?: ""
      val nfse = pedido.serie ?: ""
      saci.desfazDevolucaoSTKMOV(loja, nfno, nfse, tipoNota)
    }
    else saci.desfazPedidoSTKMOV(loja, numPedido, tipo, tipoNota)
  }

  companion object {
    const val DEVOLUCAO = "DEVOLUCAO"
  }
}