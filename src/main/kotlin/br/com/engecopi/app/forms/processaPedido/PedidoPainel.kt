package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.saci.beans.Pedido
import br.com.engecopi.saci.beans.StatusPedido
import br.com.engecopi.saci.beans.StatusPedido.*
import br.com.engecopi.saci.saci
import br.com.engecopi.utils.format
import com.github.mvysny.karibudsl.v8.horizontalLayout
import com.github.mvysny.karibudsl.v8.isMargin
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.textField
import com.vaadin.ui.CssLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme

class PedidoPainel : CssLayout() {
  private fun textReadOnly(caption: String): TextField {
    return textField(caption) {
      isReadOnly = true
      setWidth("100%")
    }
  }

  private val lojaPedido = textReadOnly("Loja")
  private val numeroPedido = textReadOnly("Número")
  private val dataPedido = textReadOnly("Data")
  private val usuarioPedido = textReadOnly("Usuário")
  private val clientePedido = textReadOnly("Cliente")
  private val notaPedido = textReadOnly("NF")
  private val statusPedido = textReadOnly("Status")

  fun setPedido(pedido: Pedido?, filtroPedido : FiltroPedido) {

    lojaPedido.value = pedido?.loja?.toString() ?: ""
    numeroPedido.value = pedido?.numero ?: ""
    dataPedido.value = pedido?.date?.format() ?: ""
    usuarioPedido.value = pedido?.username ?: ""
    clientePedido.value = pedido?.cliente ?: ""
    val nota = pedido?.notaFiscal(filtroPedido.tipoMov, filtroPedido.tipoNota)
    notaPedido.value = when {
      nota == null           -> ""
      nota.cancelado == true -> ""
      else                   -> nota.numero
    }
    statusPedido.value = when(pedido?.status) {
      NAO_PROCESSADO -> "Não Processado"
      JA_PROCESSADO  -> "Já Processado"
      else                       -> ""
    }
  }

  init {
    caption = "Pedido"
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      setWidth("100%")
      horizontalLayout {
        setWidth("100%")
        isMargin = true
        numeroPedido.addStyleName("align-right")
        addComponents(
          lojaPedido,
          numeroPedido,
          dataPedido,
          notaPedido,
          usuarioPedido,
          clientePedido,
          statusPedido
                     )

        setExpandRatio(lojaPedido, 1f)
        setExpandRatio(numeroPedido, 2f)
        setExpandRatio(dataPedido, 2f)
        setExpandRatio(notaPedido, 2f)
        setExpandRatio(usuarioPedido, 2f)
        setExpandRatio(clientePedido, 4f)
        setExpandRatio(statusPedido, 2f)
      }
    }
  }
}