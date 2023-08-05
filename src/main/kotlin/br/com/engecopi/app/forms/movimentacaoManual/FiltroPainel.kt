package br.com.engecopi.app.forms.movimentacaoManual

import br.com.engecopi.app.model.BaseProduto
import br.com.engecopi.saci.saci
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.ui.Alignment
import com.vaadin.ui.CssLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme

class FiltroPainel(
  private val movimentacaoManualPainel: MovimentacaoManualPainel,
  private val gridPainel: GridPainel
) :
  CssLayout() {
  private lateinit var edtTipos: TextField
  private lateinit var pedido: TextField
  private lateinit var edtFornecedores: TextField
  private lateinit var codigo: TextField
  private lateinit var centroLucro: TextField
  private lateinit var descricao: TextField

  init {
    caption = "Produto"
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL

    this.horizontalLayout {
      this.w = 100.perc
      this.isMargin = true

      codigo = textField("Código Produto") {
        isExpanded = false

        onEnterPressed {
          atualizaProdutos()
        }
      }

      pedido = textField("Pedido") {
        isExpanded = false

        onEnterPressed {
          atualizaProdutos()
        }
      }

      descricao = textField("Descrição") {
        this.setWidthFull()
        isExpanded = true

        onEnterPressed {
          atualizaProdutos()
        }
      }
      centroLucro = textField("Centro Lucro") {
        isExpanded = false

        onEnterPressed {
          atualizaProdutos()
        }
      }
      edtFornecedores = textField("Fornecedores") {
        this.setWidthFull()
        isExpanded = true

        onEnterPressed {
          atualizaProdutos()
        }
      }
      edtTipos = textField("Tipos") {
        this.setWidthFull()
        isExpanded = true

        onEnterPressed {
          atualizaProdutos()
        }
      }
      button("Limpar") {
        this.alignment = Alignment.BOTTOM_RIGHT
        this.isExpanded = false

        onLeftClick {
          gridPainel.limpaProdutos()
        }
      }
    }
  }

  private fun atualizaProdutos() {
    val base = baseDados()
    val produtos = saci.buscaProdutos(base)
    gridPainel.setItens(produtos)
  }

  private fun baseDados() = BaseProduto(
    loja = movimentacaoManualPainel.filtroBean().loja?.numero ?: 0,
    codprd = codigo.value?.trim() ?: "",
    fornecedores = edtFornecedores.value ?: "",
    types = edtTipos.value ?: "",
    cl = centroLucro.value ?: "",
    descricao = descricao.value ?: "",
    pedido = pedido.value ?: "",
  )
}