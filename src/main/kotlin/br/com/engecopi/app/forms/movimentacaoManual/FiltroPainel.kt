package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.BaseProduto
import br.com.engecopi.saci.saci
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme

class FiltroPainel(val movimentacaoManualPainel: MovimentacaoManualPainel, val gridPainel: GridPainel) : CssLayout() {
  private lateinit var edtTipos: TextField
  private lateinit var edtFornecedores: TextField
  private lateinit var codigo: TextField

  init {
    caption = "Produto"
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL

    this.horizontalLayout {
      this.w = 100.perc
      this.isMargin = true
      codigo = textField("Código Produto") {
        isExpanded = false
      }
      edtFornecedores = textField("Fornecedores") {
        this.setWidthFull()
        isExpanded = true
      }
      edtTipos = textField("Tipos") {
        this.setWidthFull()
        isExpanded = true
      }
      button("Busca") {
        this.alignment = Alignment.BOTTOM_RIGHT
        this.isExpanded = false

        onLeftClick(::clickBusca)
      }
    }
  }

  private fun clickBusca(clickEvent: Button.ClickEvent) {
    atualizaProdutos()
  }

  private fun atualizaProdutos() {
    val produtos = saci.buscaProdutos(baseDados())
    gridPainel.setItens(produtos)
  }

  fun baseDados() = BaseProduto(
    loja = movimentacaoManualPainel.loja.value?.numero ?: 0,
    codprd = codigo.value?.trim() ?: "",
    fornecedores = edtFornecedores.value ?: "",
    types = edtTipos.value ?: ""
  )
}