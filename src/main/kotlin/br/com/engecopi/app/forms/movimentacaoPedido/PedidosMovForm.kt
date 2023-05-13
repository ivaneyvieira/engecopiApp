package br.com.engecopi.app.forms.movimentacaoPedido

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
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Notification.Type.ERROR_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.VerticalLayout
import de.steinwedel.messagebox.ButtonOption
import de.steinwedel.messagebox.MessageBox

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
    filtro.listPedido ?: fail("Numero do pedido/nota não informado")
    val listPedido = filtro.findPedidos()

    val pedido = listPedido.montaPedido() ?: fail("Numero do pedido/nota não encontrado")

    filtro.tipoNota ?: fail("Tipo da Nota não informado")

    if (pedido.tipo != PEDIDO) when {
      filtro.tipoMov == ENTRADA && pedido.tipo == COMPRA -> fail("A nota não é de saída")
      filtro.tipoMov == SAIDA && pedido.tipo == DEVOLUCAO -> fail("A nota não é de entrada")
    }

    pedidoPainel.setPedido(null, filtro)
    setProdutosGrid(emptyList())

    when {
      !pedido.isDataValida() -> {
        fail("Pedido tem mais de 366 dias")
      }
      !pedido.isLojaValida() -> {
        fail("O cliente da nota/pedidos não é ${loja?.numero}")
      }
      !pedido.produtoValido() -> {
        fail("O pedido possui um produto com código maior que 980000")
      }
    }
    pedidoPainel.setPedido(pedido, filtro)
    setProdutosGrid(listPedido)
  }

  private fun <T, V> List<T>.merge(item: T.() -> V): V? {
    return this.mapNotNull { it.item() }.distinct().firstOrNull()
  }

  private fun List<PedidoNota>.montaPedido(): PedidoNota? {
    return if (this.isEmpty()) null
    else {
      PedidoNota(
          storeno = this.merge { storeno },
          numero = this.merge { numero },
          date = this.mapNotNull { it.date }.maxOrNull(),
          userno = this.merge { userno },
          username = this.merge { username },
          cpf_cgc = this.merge { cpf_cgc },
          cliente = this.merge { cliente },
          status = this.merge { status },
          tipo = this.merge { tipo },
          storeno_custno = this.merge { storeno_custno } ?: 0,
      )
    }
  }

  private fun execProcessa(filtro: FiltroPedido) {
    val loja = filtro.loja ?: fail("Loja não informada")
    val listPedido = filtro.listPedido ?: fail("Numero do pedido/nota não informado")
    val listPedidos = filtro.findPedidos()
    val pedido = listPedidos.montaPedido() ?: fail("Numero do pedido/nota não encontrado")
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
    val nota = saci.pesquisaNotaSTKMOV(loja, listPedido, tipo, tipoNota)

    if (pedido.status != NAO_PROCESSADO && nota != null && nota.cancelado != true) fail("Nota já processada")
    else {
      if (pedido.isData30Dias()) {
        processa(listPedidos, tipo, tipoNota)
        filtroPedidoPainel.execFiltro(filtro)
      } else {
        messageConfirma("O pedido tem mais de 30 dias. Confirma?") {
          processa(listPedidos, tipo, tipoNota)
          filtroPedidoPainel.execFiltro(filtro)
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

  private fun desfazProcessa(filtro: FiltroPedido) {
    val loja = filtro.loja ?: fail("Loja Não encontrada")
    val listPedido = filtro.findPedidos()
    val tipo = filtro.tipoMov ?: fail("Tipo de movimento não informado")
    val tipoNota = filtro.tipoNota ?: fail("Tipo de nota não Informada")
    listPedido.firstOrNull() ?: fail("Pedido não encontrado")
    val nota = saci.pesquisaNotaSTKMOV(loja, filtro.listPedido, tipo, tipoNota)
    if (nota == null || nota.cancelado == true) fail("Esse pedido não foi processado")

    desfaz(listPedido, tipo, tipoNota)
    filtroPedidoPainel.execFiltro(filtro)
  }

  private fun setProdutosGrid(list: List<PedidoNota>) {
    val produtos = list.flatMap {
      it.produtos()
    }
    gridPainel.grid.dataProvider = ListDataProvider(produtos)
  }

  private fun processa(listPedidos: List<PedidoNota>, tipo: TipoMov, tipoNota: TipoNota) {
    listPedidos.forEach { pedido ->
      processa(pedido, tipo, tipoNota)
    }
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

  private fun desfaz(listPedidoNota: List<PedidoNota>, tipoMov: TipoMov, tipoNota: TipoNota) {
    listPedidoNota.forEach { pedido ->
      desfaz(pedido, tipoMov, tipoNota)
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