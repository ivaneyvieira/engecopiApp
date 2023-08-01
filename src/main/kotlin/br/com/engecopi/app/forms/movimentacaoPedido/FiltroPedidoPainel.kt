package br.com.engecopi.app.forms.movimentacaoPedido

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.app.model.TipoNota.GARANTIA
import br.com.engecopi.app.model.TipoNota.PERDA
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.ui.Alignment.BOTTOM_LEFT
import com.vaadin.ui.Alignment.BOTTOM_RIGHT
import com.vaadin.ui.CssLayout
import com.vaadin.ui.themes.ValoTheme

class FiltroPedidoPainel(
  val execFiltro: (FiltroPedido) -> Unit,
  val execProcessa: (FiltroPedido) -> Unit,
  val desfazProcessa: (FiltroPedido) -> Unit
) : CssLayout() {
  private val binderFiltroPedido = beanValidationBinder<FiltroPedido>()
  private var filtroPedido: FiltroPedido? = FiltroPedido()

  val tipoNota = comboBox<TipoNota>("Tipo Nota") {
    setItems(TipoNota.values().toList().sortedBy { it.numero })
    isEmptySelectionAllowed = false
    isTextInputAllowed = false
    setItemCaptionGenerator {
      when (it) {
        GARANTIA -> "Garantia"
        PERDA -> "Perda"
        else -> ""
      }
    }
    setWidth("120px")
    value = PERDA
    bind(binderFiltroPedido).bind(FiltroPedido::tipoNota)
  }

  val loja = comboBox<Loja>("Loja") {
    isEmptySelectionAllowed = false
    isTextInputAllowed = false
    setItems(Loja.values().toList())
    setItemCaptionGenerator { it.numero.toString() + " - " + it.descricao }
    setWidth("150px")
    bind(binderFiltroPedido).bind(FiltroPedido::loja)
  }

  private val numPedido = textField("Pedido/Nota") {
    addStyleName("align-right")
    bind(binderFiltroPedido).withValidator({ it != null }, "Pedido com valor nulo").bind(FiltroPedido::numPedido)
  }

  private val tipoMov = radioButtonGroup<TipoMov>("Tipo") {
    styleName = ValoTheme.OPTIONGROUP_HORIZONTAL

    setItems(TipoMov.values().toList())
    setItemIconGenerator { it.icon }
    bind(binderFiltroPedido).bind(FiltroPedido::tipoMov)
  }

  private val btnPesquisa = button("Pesquisa") {
    addClickListener {
      if (binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          execFiltro(it)
          binderFiltroPedido.readBean(it)
        }
      }
    }
  }

  private val btnProcessa = button("Processamento") {
    addClickListener {
      val filtro = filtroPedido ?: return@addClickListener
      if (binderFiltroPedido.writeBeanIfValid(filtro)) {
        execProcessa(filtro)
        binderFiltroPedido.readBean(filtro)
      }
    }
  }

  private val btnDesfazProcessa = button("Desfaz") {
    addClickListener {
      val filtro = filtroPedido ?: return@addClickListener
      if (binderFiltroPedido.writeBeanIfValid(filtro)) {
        desfazProcessa(filtro)
        binderFiltroPedido.readBean(filtro)
      }
    }
  }

  init {
    caption = "Filtro"
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      horizontalLayout {
        setWidth("100%")
        isMargin = true
        addComponents(tipoNota, loja, numPedido, tipoMov, btnPesquisa, btnProcessa, btnDesfazProcessa)
        setExpandRatio(btnProcessa, 1f)
        setComponentAlignment(btnProcessa, BOTTOM_LEFT)
        setComponentAlignment(btnPesquisa, BOTTOM_LEFT)
        setComponentAlignment(btnDesfazProcessa, BOTTOM_RIGHT)
      }
    }
    binderFiltroPedido.readBean(filtroPedido)
  }
}