package br.com.engecopi.app.forms.defazMovimentacao

import br.com.engecopi.app.model.FiltroMov
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.app.model.TipoNota.GARANTIA
import br.com.engecopi.app.model.TipoNota.PERDA
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.ui.Alignment.BOTTOM_LEFT
import com.vaadin.ui.CssLayout
import com.vaadin.ui.themes.ValoTheme

class DefazMovimentacaoPainel(
  val execProcessa: () -> Unit,
  val execDesfaz: () -> Unit,
) : CssLayout() {
  private val tipoNota = comboBox<TipoNota>("Tipo Nota") {
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
  }

  private val loja = comboBox<Loja>("Loja") {
    isEmptySelectionAllowed = false
    isTextInputAllowed = false
    setItems(Loja.values().toList())
    setItemCaptionGenerator { it.numero.toString() + " - " + it.descricao }
    setWidth("150px")
    value = Loja.MF
  }

  private val transacao = textField("Transacao") {
    addStyleName("align-right")
  }

  private val tipoMov = radioButtonGroup<TipoMov>("Tipo") {
    styleName = ValoTheme.OPTIONGROUP_HORIZONTAL

    setItems(TipoMov.values().toList())
    setItemIconGenerator { it.icon }
    value = TipoMov.SAIDA
  }

  fun filtroBean(): FiltroMov = FiltroMov(
    tipoMov = tipoMov.value,
    tipoNota = tipoNota.value,
    loja = loja.value,
    transacao = transacao.value ?: "",
  )

  fun setTransacao(transacaoNumber: String) {
    transacao.value = transacaoNumber
  }

  private val btnProcessa = button("Processamento") {
    addClickListener {
      execProcessa()
    }
  }

  private val btnDesfaz = button("Desfaz") {
    addClickListener {
      execDesfaz()
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
        addComponents(tipoNota, loja, tipoMov, btnProcessa, transacao, btnDesfaz)
        setExpandRatio(btnProcessa, 1f)
        setExpandRatio(btnDesfaz, 1f)
        setComponentAlignment(btnProcessa, BOTTOM_LEFT)
        setComponentAlignment(btnDesfaz, BOTTOM_LEFT)
      }
    }
  }
}