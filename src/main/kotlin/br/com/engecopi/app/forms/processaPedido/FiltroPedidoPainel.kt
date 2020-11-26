package br.com.engecopi.app.forms.processaPedido

import br.com.engecopi.app.model.FiltroPedido
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import com.github.mvysny.karibudsl.v8.beanValidationBinder
import com.github.mvysny.karibudsl.v8.bind
import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.comboBox
import com.github.mvysny.karibudsl.v8.horizontalLayout
import com.github.mvysny.karibudsl.v8.isMargin
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.radioButtonGroup
import com.github.mvysny.karibudsl.v8.textField
import com.vaadin.ui.Alignment
import com.vaadin.ui.CssLayout
import com.vaadin.ui.themes.ValoTheme

class FiltroPedidoPainel: CssLayout() {
  private val binderFiltroPedido = beanValidationBinder<FiltroPedido>()
  private var filtroPedido: FiltroPedido? = FiltroPedido()
  lateinit var execFiltro: (FiltroPedido) -> Unit
  lateinit var execProcessa: (FiltroPedido) -> Unit
  lateinit var desfazProcessa: (FiltroPedido) -> Unit
  val tipoMov = radioButtonGroup<TipoMov>("Tipo") {
    styleName = ValoTheme.OPTIONGROUP_HORIZONTAL
    
    setItems(TipoMov.values().toList())
    setItemIconGenerator {it.icon}
    bind(binderFiltroPedido).bind(FiltroPedido::tipoMov)
  }
  val tipoNota = comboBox<Int>("Tipo Nota") {
    setItems(9, 7)
    setItemCaptionGenerator {
      when(it) {
        7    -> "Garantia"
        9    -> "Perda"
        else -> ""
      }
    }
    setWidth("120px")
    value = 9
    bind(binderFiltroPedido).bind(FiltroPedido::tipoNota)
  }
  val loja = comboBox<Loja>("Loja") {
    isEmptySelectionAllowed = false
    isTextInputAllowed = false
    setItems(Loja.values().toList())
    setItemCaptionGenerator {it.numero.toString() + " - " + it.descricao}
    setWidth("150px")
    bind(binderFiltroPedido).bind(FiltroPedido::loja)
  }
  val numPedido = textField("Pedido/Nota") {
    addStyleName("align-right")
    bind(binderFiltroPedido)
      .withValidator({it != null}, "Pedido com valor nulo")
      .bind(FiltroPedido::numPedido)
  }
  val btnPesquisa = button("Pesquisa") {
    addClickListener {
      if(binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          execFiltro(it)
          binderFiltroPedido.readBean(it)
        }
      }
    }
  }
  val btnProcessa = button("Processamento") {
    addClickListener {
      if(binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          execProcessa(it)
          binderFiltroPedido.readBean(it)
        }
      }
    }
  }
  val btnDesfazProcessa = button("Desfaz") {
    addClickListener {
      if(binderFiltroPedido.writeBeanIfValid(filtroPedido)) {
        filtroPedido?.let {
          desfazProcessa(it)
          binderFiltroPedido.readBean(it)
        }
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
        addComponents(tipoMov, tipoNota, loja, numPedido, btnPesquisa, btnProcessa, btnDesfazProcessa)
        setExpandRatio(numPedido, 1f)
        setComponentAlignment(btnProcessa, Alignment.BOTTOM_RIGHT)
        setComponentAlignment(btnPesquisa, Alignment.BOTTOM_RIGHT)
        setComponentAlignment(btnDesfazProcessa, Alignment.BOTTOM_RIGHT)
      }
    }
    binderFiltroPedido.readBean(filtroPedido)
  }
}