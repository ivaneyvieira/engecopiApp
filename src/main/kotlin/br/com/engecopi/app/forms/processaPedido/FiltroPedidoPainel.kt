package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import com.github.vok.karibudsl.beanValidationBinder
import com.github.vok.karibudsl.bind
import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.comboBox
import com.github.vok.karibudsl.horizontalLayout
import com.github.vok.karibudsl.isMargin
import com.github.vok.karibudsl.panel
import com.github.vok.karibudsl.radioButtonGroup
import com.github.vok.karibudsl.textField
import com.vaadin.data.converter.StringToIntegerConverter
import com.vaadin.ui.Alignment
import com.vaadin.ui.CssLayout
import com.vaadin.ui.themes.ValoTheme

class FiltroPedidoPainel : CssLayout() {
  private val binderFiltroPedido = beanValidationBinder<FiltroPedido>()
  private var filtroPedido: FiltroPedido? = FiltroPedido()
  lateinit var execFiltro: (FiltroPedido) -> Unit
  lateinit var execProcessa: (FiltroPedido) -> Unit
  lateinit var desfazProcessa: (FiltroPedido) -> Unit
  
  val tipoMov = radioButtonGroup<TipoMov>("Tipo:") {
    styleName = ValoTheme.OPTIONGROUP_HORIZONTAL
    
    setItems(TipoMov.values().toList())
    setItemIconGenerator { it.icon }
    bind(binderFiltroPedido).bind(FiltroPedido::tipoMov)
  }
  
  val loja = comboBox<Loja>("Loja") {
    setWidth("300px")
    isEmptySelectionAllowed = false
    isTextInputAllowed = false
    setItems(Loja.values().toList())
    setItemCaptionGenerator { it.numero.toString() + " - " + it.descricao }
    bind(binderFiltroPedido).bind(FiltroPedido::loja)
  }
  
  val numPedido = textField("Pedido") {
    addStyleName("align-right")
    bind(binderFiltroPedido).withConverter(StringToIntegerConverter("Pedido inválido"))
            .withValidator({ it != null }, "Pedido com valor nulo").bind(FiltroPedido::numPedido)
  }
  
  val btnPesquisa = button("Pesquisa", {
    addClickListener {
      if (binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          execFiltro(it)
        }
      }
    }
  })
  
  val btnProcessa = button("Processamento", {
    addClickListener {
      if (binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          execProcessa(it)
        }
      }
    }
  })
  
  val btnDesfazProcessa = button("Desfaz", {
    addClickListener {
      if (binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          desfazProcessa(it)
        }
      }
    }
  })
  
  init {
    caption = "Filtro"
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      horizontalLayout {
        setWidth("100%")
        isMargin = true
        addComponents(tipoMov, loja, numPedido, btnPesquisa, btnProcessa, btnDesfazProcessa)
        setExpandRatio(numPedido, 1f)
        setComponentAlignment(btnProcessa, Alignment.BOTTOM_RIGHT)
        setComponentAlignment(btnPesquisa, Alignment.BOTTOM_RIGHT)
        setComponentAlignment(btnDesfazProcessa, Alignment.BOTTOM_RIGHT)
      }
    }
    binderFiltroPedido.readBean(filtroPedido)
  }
}