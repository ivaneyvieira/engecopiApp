package br.com.engecopi.app.forms.defazMovimentacao

import br.com.engecopi.app.model.FiltroTransacao
import br.com.engecopi.app.model.Loja
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.ui.Alignment.BOTTOM_LEFT
import com.vaadin.ui.CssLayout
import com.vaadin.ui.themes.ValoTheme

class DefazMovimentacaoPainel(
  val execBusca: () -> Unit,
  val execProcessa: () -> Unit,
) : CssLayout() {
  private val loja = comboBox<Loja>("Loja") {
    isEmptySelectionAllowed = false
    isTextInputAllowed = false
    setItems(Loja.values().toList())
    setItemCaptionGenerator { it.numero.toString() + " - " + it.descricao }
    setWidth("150px")
  }

  private val transacao = textField("Transacao") {
    addStyleName("align-right")
  }

  private val ajustaSaldo = checkBox("Ajusta Saldo") {
    value = true
  }

  fun filtroTransacao() = FiltroTransacao(
    loja = loja.value,
    transacao = transacao.value ?: "",
    ajustaSaldo = ajustaSaldo.value ?: false
  )

  private val btnProcessa = button("Remove") {
    addClickListener {
      execProcessa()
    }
  }

  private val btnBusca = button("Busca") {
    addClickListener {
      execBusca()
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
        addComponents(loja, transacao, btnBusca, ajustaSaldo, btnProcessa)
        setExpandRatio(btnBusca, 1f)
        setComponentAlignment(btnBusca, BOTTOM_LEFT)
        setComponentAlignment(ajustaSaldo, BOTTOM_LEFT)
        setComponentAlignment(btnProcessa, BOTTOM_LEFT)
      }
    }
  }
}