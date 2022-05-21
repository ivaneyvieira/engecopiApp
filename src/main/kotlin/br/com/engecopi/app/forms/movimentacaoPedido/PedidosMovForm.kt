package br.com.engecopi.app.forms.movimentacaoPedido

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.Loja
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
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Notification.Type.ERROR_MESSAGE
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

  fun fail(msg: String): Nothing {
    show(msg, ERROR_MESSAGE)
    throw Exception(msg)
  }

  private fun execFiltro(filtro: FiltroPedido) {
    val loja = filtro.loja
    val numPedido = filtro.numPedido ?: fail("Numero do pedido/nota não informado")
    val pedidoNota = saci.pedidoNota(loja, numPedido) ?: fail("Numero do pedido/nota não encontrado")

    val tipoNota = filtro.tipoNota ?: fail("Tipo da Nota não informado")

    if (pedidoNota.tipo != PEDIDO) when {
      filtro.tipoMov == ENTRADA && pedidoNota.tipo == COMPRA  -> fail("A nota não é de saída")
      filtro.tipoMov == SAIDA && pedidoNota.tipo == DEVOLUCAO -> fail("A nota não é de entrada")
    }

    pedidoPainel.setPedido(null, filtro)
    setProdutosGrid(null)

    when {
      !pedidoNota.isDataValida()  -> {
        fail("Pedido tem mais de 366 dias")
      }
      !pedidoNota.isLojaValida()  -> {
        fail("O cliente da nota/pedidos não é ${loja?.numero}")
      } // tipoNota == GARANTIA && pedidoNota.isEngecopi() -> {
      //   fail("O cliente não pode ser loja")
      // }
      //  tipoNota == PERDA && !pedidoNota.isEngecopi() -> {
      //    fail("O cliente deve ser uma loja")
      //  }
      !pedidoNota.produtoValido() -> {
        fail("O pedido possui um produto com código maior que 980000")
      }
    }
    pedidoPainel.setPedido(pedidoNota, filtro)
    setProdutosGrid(pedidoNota)
  }

  private fun execProcessa(filtro: FiltroPedido) {
    val loja = filtro.loja ?: fail("Loja não informada")
    val numPedido = filtro.numPedido ?: fail("Numero do pedido/nota não informado")
    val pedidoNota = saci.pedidoNota(loja, numPedido) ?: fail("Numero do pedido/nota não encontrado")
    val tipoNota = filtro.tipoNota ?: fail("O Tipo da nota não foi informada")
    val pedidoNotaTipo = pedidoNota.tipo

    if (pedidoNotaTipo != PEDIDO) when {
      filtro.tipoMov != ENTRADA || pedidoNotaTipo != DEVOLUCAO -> fail("A nota não é de saida")
      filtro.tipoMov != SAIDA || pedidoNotaTipo != COMPRA      -> fail("A nota não é de entrada")
    }
    val tipo = filtro.tipoMov ?: fail("Tipo do Movimento não foi informado")
    when {
      !pedidoNota.isDataValida() -> {
        fail("Pedido tem mais de 366 dias")
      }
      !pedidoNota.isLojaValida() -> {
        fail("O cliente da nota/pedidos não é ${loja.numero}")
      } //tipoNota == GARANTIA && pedidoNota.isEngecopi() -> {
      //   fail("O cliente não pode ser loja")
      //} //tipoNota == PERDA && !pedidoNota.isEngecopi()   -> {
      //   fail("O cliente deve ser uma loja")
      // }
    }
    val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo, tipoNota)

    if (pedidoNota.status != NAO_PROCESSADO && nota != null && nota.cancelado != true) fail("Nota já processada")
    else {
      processa(pedidoNota, loja, numPedido, tipo, tipoNota)
      filtroPedidoPainel.execFiltro(filtro)
    }
  }

  private fun desfazProcessa(filtro: FiltroPedido) {
    val loja = filtro.loja ?: fail("Loja Não encontrada")
    val numPedido = filtro.numPedido ?: ""
    val tipo = filtro.tipoMov ?: fail("Tipo de movimento não informado")
    val tipoNota = filtro.tipoNota ?: fail("Tipo de nota não Informada")
    val pedido = saci.pedidoNota(loja, numPedido) ?: fail("Pedido não encontrado")
    val nota = saci.pesquisaNotaSTKMOV(loja, numPedido, tipo, tipoNota)
    if (nota == null || nota.cancelado == true) fail("Esse pedido não foi processado")

    desfaz(pedido, loja, numPedido, tipo, tipoNota)
    filtroPedidoPainel.execFiltro(filtro)
  }

  private fun setProdutosGrid(pedidoNota: PedidoNota?) {
    val produtos = pedidoNota?.produtos().orEmpty()
    gridPainel.grid.dataProvider = ListDataProvider(produtos)
  }

  private fun processa(pedidoNota: PedidoNota, loja: Loja, numPedido: String, tipo: TipoMov, tipoNota: TipoNota) {
    when (tipoNota) {
      GARANTIA -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.processaNota(loja, nfno, nfse, NF)
        }
        else saci.processaPedido(loja, numPedido, tipo, tipoNota, NF)
      }
      PERDA    -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.processaNota(loja, nfno, nfse, STKMOV)
        }
        else saci.processaPedido(loja, numPedido, tipo, tipoNota, STKMOV)
      }
    }
  }

  private fun desfaz(pedidoNota: PedidoNota, loja: Loja, numPedido: String, tipoMov: TipoMov, tipoNota: TipoNota) {
    when (tipoNota) {
      GARANTIA -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.desfazNota(loja, nfno, nfse, NF)
        }
        else saci.desfazPedido(loja, numPedido, tipoMov, NF)
      }
      PERDA    -> {
        if (pedidoNota.tipo == DEVOLUCAO || pedidoNota.tipo == COMPRA) {
          val nfno = pedidoNota.numeroPedido ?: ""
          val nfse = pedidoNota.serie ?: ""
          saci.desfazNota(loja, nfno, nfse, STKMOV)
        }
        else saci.desfazPedido(loja, numPedido, tipoMov, STKMOV)
      }
    }
  }
}