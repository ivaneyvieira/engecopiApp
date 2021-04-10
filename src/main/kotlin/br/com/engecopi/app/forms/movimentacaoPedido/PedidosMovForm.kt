package br.com.engecopi.app.forms.movimentacaoPedido

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.app.model.TipoNota.GARANTIA
import br.com.engecopi.app.model.TipoNota.PERDA
import br.com.engecopi.saci.beans.Pedido
import br.com.engecopi.saci.beans.StatusPedido.NAO_PROCESSADO
import br.com.engecopi.saci.beans.TipoPedido.DEVOLUCAO
import br.com.engecopi.saci.saci
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Notification.Type.WARNING_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.VerticalLayout

class PedidosMovForm : VerticalLayout() {
  private val filtroPedidoPainel = FiltroPedidoPainel(::execFiltro, ::execProcessa, ::desfazProcessa)
  private val pedidoPainel = PedidoPainel()
  private val gridPainel = GridPainel()

  init {
    setSizeFull()
    addComponents(filtroPedidoPainel, pedidoPainel)
    addComponentsAndExpand(gridPainel)
  }

  fun fail(msg : String): Nothing{
    show(msg)
    throw Exception(msg)
  }

  private fun execFiltro(filtro: FiltroPedido) {
    val loja = filtro.loja ?: fail("Loja não informada")
    val numPedido = filtro.numPedido ?:  fail("Numero do pedido/nota não informado")
    val pedido = saci.pedidoNota(loja, numPedido) ?:  fail("Numero do pedido/nota não encontrado")

    pedido.let { ped ->
      if (ped.tipo == DEVOLUCAO) {
        filtro.tipoMov = ENTRADA
        filtro.tipoNota = GARANTIA
      }
    }
    val tipoNota = filtro.tipoNota
    val pedidoValido = validaPedido(pedido)
    pedidoPainel.setPedido(pedidoValido, filtro)
    when {
      pedidoValido == null -> {
        show("Esse pedido não foi encontrado", WARNING_MESSAGE)
      }

      tipoNota == GARANTIA && pedidoValido.isEngecopi() -> {
        show("O cliente não pode ser loja", WARNING_MESSAGE)
      }

      tipoNota == PERDA && !pedidoValido.isEngecopi() -> {
        show("O cliente deve ser uma loja", WARNING_MESSAGE)
      }

      !pedidoValido.produtoValido() -> {
        show("O pedido possui um produto com código maior que 980000", WARNING_MESSAGE)
      }
    }
    setProdutosGrid(pedidoValido)
  }

  private fun execProcessa(filtro: FiltroPedido) {
    val loja = filtro.loja
    val numPedido = filtro.numPedido ?: ""
    val pedido = saci.pedidoNota(loja, numPedido)

    pedido?.let { ped ->
      if (ped.tipo == DEVOLUCAO) {
        filtro.tipoMov = ENTRADA
        filtro.tipoNota = GARANTIA
      }
    }
    val tipo = filtro.tipoMov
    val tipoNota = filtro.tipoNota
    val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo, tipoNota)
    val pedidoValido = validaPedido(pedido)

    when {
      pedidoValido == null -> {
        show("Esse pedido não foi encontrado", WARNING_MESSAGE)
      }

      tipoNota == GARANTIA && pedidoValido.isEngecopi() -> {
        show("O cliente não pode ser loja", WARNING_MESSAGE)
      }

      tipoNota == PERDA && !pedidoValido.isEngecopi() -> {
        show("O cliente deve ser uma loja", WARNING_MESSAGE)
      }

      pedidoValido.status == NAO_PROCESSADO -> {
        processa(pedido, loja, numPedido, tipo, tipoNota)
        filtroPedidoPainel.execFiltro(filtro)
      }

      else -> {
        when {
          nota == null -> {
            processa(pedido, loja, numPedido, tipo, tipoNota)
            filtroPedidoPainel.execFiltro(filtro)
          }

          nota.cancelado == true -> {
            processa(pedido, loja, numPedido, tipo, tipoNota)
            filtroPedidoPainel.execFiltro(filtro)
          }

          else -> {
            show("Nota já processada", WARNING_MESSAGE)
          }
        }
      }
    }
  }

  private fun desfazProcessa(filtro: FiltroPedido) {
    val loja = filtro.loja
    val numPedido = filtro.numPedido ?: ""
    val tipo = filtro.tipoMov
    val tipoNota = filtro.tipoNota
    val pedido = saci.pedidoNota(loja, numPedido)
    val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo, tipoNota)
    when (pedido) {
      null -> {
        show("Esse pedido não foi encontrado", WARNING_MESSAGE)
      }

      else -> {
        when {
          nota == null -> {
            show("Esse pedido não foi processado", WARNING_MESSAGE)
          }

          nota.cancelado == true -> {
            show("Esse pedido não foi processado", WARNING_MESSAGE)
          }

          else -> {
            desfaz(pedido, loja, numPedido, tipo, tipoNota)
          }
        }
        filtroPedidoPainel.execFiltro(filtro)
      }
    }
  }

  private fun validaPedido(pedido: Pedido?): Pedido? {
    val lojaNome = pedido?.loja?.descricao
    return when {
      pedido == null -> {
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

      else -> pedido
    }
  }

  private fun setProdutosGrid(pedido: Pedido?) {
    val produtos = pedido?.produtos().orEmpty()
    gridPainel.grid.dataProvider = ListDataProvider(produtos)
  }

  private fun processa(
    pedido: Pedido?, loja: Loja?, numPedido: String, tipo: TipoMov?, tipoNota: TipoNota?
                      ) {
    if (pedido?.tipo == DEVOLUCAO) {
      val nfno = pedido.numeroPedido ?: ""
      val nfse = pedido.serie ?: ""
      saci.processaDevolucaoSTKMOV(loja, nfno, nfse, GARANTIA)
    }
    else saci.processaPedidoSTKMOV(loja, numPedido, tipo, tipoNota)
  }

  private fun desfaz(
    pedido: Pedido?, loja: Loja?, numPedido: String, tipoMov: TipoMov?, tipoNota: TipoNota?
                    ) {
    if (pedido?.tipo == DEVOLUCAO) {
      val nfno = pedido.numeroPedido ?: ""
      val nfse = pedido.serie ?: ""
      saci.desfazDevolucaoSTKMOV(loja, nfno, nfse, GARANTIA)
    }
    else saci.desfazPedidoSTKMOV(loja, numPedido, tipoMov, tipoNota)
  }
}