package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.saci.beans.Pedido
import br.com.engecopi.saci.saci
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Notification
import com.vaadin.ui.Notification.Type.WARNING_MESSAGE
import com.vaadin.ui.VerticalLayout

class PedidosMovForm: VerticalLayout() {
  val filtroPedidoPainel = FiltroPedidoPainel()
  val pedidoPainel = PedidoPainel()
  val gridPainel = GridPainel()
  
  init {
    filtroPedidoPainel.execFiltro = {filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)
  
      pedido?.let {ped ->
        if(ped.tipo == DEVOLUCAO) {
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
          Notification.show("Esse pedido não foi encontrado", Notification.Type.WARNING_MESSAGE)
        }
        tipoNota == 7 && pedidoValido.isEngecopi()  -> {
          Notification.show("O cliente não pode ser loja", Notification.Type.WARNING_MESSAGE)
        }
        tipoNota == 9 && !pedidoValido.isEngecopi() -> {
          Notification.show("O cliente deve ser uma loja", Notification.Type.WARNING_MESSAGE)
        }
        pedidoValido.produtoValido() == false -> {
          Notification.show("O pedido possui um produto com código maior que 980000", Notification.Type.WARNING_MESSAGE)
        }
      }
      setProdutosGrid(pedidoValido)
    }
    
    filtroPedidoPainel.execProcessa = {filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)
      
      pedido?.let {ped ->
        if(ped.tipo == DEVOLUCAO) {
          filtro.tipoMov = ENTRADA
          filtro.tipoNota = 7
        }
      }
      val tipo = filtro.tipoMov?.cod ?: ""
      val tipoNota = filtro.tipoNota
      val nota = saci.pesquisaNota(loja, numPedido, tipo)
      val pedidoValido = validaPedido(pedido)
      
      when {
        pedidoValido == null                        -> {
          Notification.show("Esse pedido não foi encontrado", Notification.Type.WARNING_MESSAGE)
        }
        tipoNota == 7 && pedidoValido.isEngecopi() -> {
          Notification.show("O cliente não pode ser loja", Notification.Type.WARNING_MESSAGE)
        }
        tipoNota == 9 && !pedidoValido.isEngecopi() -> {
          Notification.show("O cliente deve ser uma loja", Notification.Type.WARNING_MESSAGE)
        }
        pedidoValido.status == 1                    -> {
          processa(pedido, loja, numPedido, tipo, tipoNota)
          filtroPedidoPainel.execFiltro(filtro)
        }
        else                     -> {
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
              Notification.show("Nota já processada", Notification.Type.WARNING_MESSAGE)
            }
          }
        }
      }
    }
    
    filtroPedidoPainel.desfazProcessa = {filtro ->
      val loja = filtro.loja?.numero ?: 0
      val numPedido = filtro.numPedido ?: ""
      val tipo = filtro.tipoMov?.cod ?: ""
      val pedido = saci.pedidoNota(loja, numPedido)
      val nota = saci.pesquisaNota(loja, numPedido, tipo)
      when(pedido) {
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
              desfaz(pedido, loja, numPedido, tipo)
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
        Notification.show("Pedido não encontrado", WARNING_MESSAGE)
        null
      }
      !pedido.isDataValida() -> {
        Notification.show("Pedido tem mais de 30 dias", WARNING_MESSAGE)
        null
      }
      !pedido.isLojaValida() -> {
        Notification.show("O cliente da nota/pedidos não é $lojaNome", WARNING_MESSAGE)
        null
      }
      else                   -> pedido
    }
  }
  
  private fun setProdutosGrid(pedido: Pedido?) {
    val produtos = pedido?.produtos()
    gridPainel.grid.dataProvider = ListDataProvider(produtos)
  }
  
  private fun processa(pedido: Pedido?,
                       loja: Int,
                       numPedido: String,
                       tipo: String,
                       tipoNota: Int) {
    if(pedido?.tipo == DEVOLUCAO) {
      val nfno = pedido.numeroPedido?.toString() ?: ""
      val nfse = pedido.serie ?: ""
      saci.processaDevolucao(loja, nfno, nfse)
    }
    else
      saci.processaPedido(loja, numPedido, tipo, tipoNota)
  }
  
  private fun desfaz(pedido: Pedido?,
                     loja: Int,
                     numPedido: String,
                     tipo: String) {
    if(pedido?.tipo == DEVOLUCAO) {
      val nfno = pedido.numeroPedido?.toString() ?: ""
      val nfse = pedido.serie ?: ""
      saci.desfazDevolucao(loja, nfno, nfse)
    }
    else
      saci.desfazPedido(loja, numPedido, tipo)
  }
  
  private fun notaHabilitada(pedido: Pedido, tipo: String?): Boolean {
    val notaFiscal = pedido.notaFiscal(tipo ?: "")
    val habilitada = notaFiscal?.let {it.cancelado == false} ?: false
    return habilitada
  }
  
  companion object {
    val DEVOLUCAO = "DEVOLUCAO"
  }
}