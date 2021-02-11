package br.com.engecopi.app.forms.ajustaEstoque

import br.com.consutec.modelo.Base
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.saci.saci
import com.github.mvysny.karibudsl.v8.alignment
import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.checkBox
import com.github.mvysny.karibudsl.v8.comboBox
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.horizontalLayout
import com.github.mvysny.karibudsl.v8.isExpanded
import com.github.mvysny.karibudsl.v8.isMargin
import com.github.mvysny.karibudsl.v8.onLeftClick
import com.github.mvysny.karibudsl.v8.perc
import com.github.mvysny.karibudsl.v8.radioButtonGroup
import com.github.mvysny.karibudsl.v8.textField
import com.github.mvysny.karibudsl.v8.w
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.CheckBox
import com.vaadin.ui.ComboBox
import com.vaadin.ui.RadioButtonGroup
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme

class HeaderPanel(val ajustaEstoqueForm: AjustaEstoqueForm): VerticalLayout() {
  private lateinit var fornecedorNF: TextField
  private lateinit var incluirNoras: CheckBox
  private lateinit var tipos: TextField
  private lateinit var fornecedores: TextField
  private lateinit var codigo: TextField
  private lateinit var tipoMov: RadioButtonGroup<TipoMov>
  private lateinit var loja: ComboBox<Loja>
  
  init {
    this.w = 100.perc
    this.isMargin = false
  
    this.horizontalLayout {
      this.w = 100.perc
      loja = comboBox("Loja2") {
        isEmptySelectionAllowed = false
        isTextInputAllowed = false
        setItems(Loja.values().toList())
        setItemCaptionGenerator {it.numero.toString() + " - " + it.descricao}
        isExpanded = false
      }
      tipoMov = radioButtonGroup<TipoMov>("Tipo") {
        styleName = ValoTheme.OPTIONGROUP_HORIZONTAL
        
        setItems(TipoMov.values().toList())
        setItemIconGenerator {it.icon}
        isExpanded = true
      }
    }
    this.horizontalLayout {
      this.w = 100.perc
      codigo = textField("Código Produto") {
        isExpanded = false
      }
      fornecedores = textField("Fornecedores") {
        this.setWidthFull()
        isExpanded = true
      }
      tipos = textField("Tipos") {
        this.setWidthFull()
        isExpanded = true
      }
    }
    this.horizontalLayout {
      this.w = 100.perc
      incluirNoras = checkBox("Incluir Notas") {
        this.alignment = Alignment.BOTTOM_LEFT
        this.isExpanded = false
      }
      fornecedorNF = textField("Fornecedor") {
        this.isExpanded = false
      }
      horizontalLayout {
        this.setWidthFull()
        this.isExpanded = true
      }
      button("Busca") {
        this.alignment = Alignment.BOTTOM_RIGHT
        this.isExpanded = false

        onLeftClick(::clickBusca)
      }
      button("Executar") {
        this.alignment = Alignment.BOTTOM_RIGHT
        this.isExpanded = false
      }
      button("Desfazer") {
        this.alignment = Alignment.BOTTOM_RIGHT
        this.isExpanded = false
      }
    }
  }
  
  fun clickBusca(clickEvent: Button.ClickEvent) {
    val produtos = saci.buscaProdutos(baseDados())
    ajustaEstoqueForm.setProdutos(produtos)
  }
  
  fun baseDados() = Base(lojaDestino = loja.value?.numero ?: 0,
                         operacao = tipoMov.value.operacao(),
                         codprd = codigo.value ?: "",
                         fornecedores = fornecedores.value ?: "",
                         tipos = tipos.value ?: "",
                         incluiNfFornecedor = incluirNoras.value ?: false,
                         fornecedorNf = fornecedorNF.value ?: "")
}

private fun TipoMov.operacao(): String {
  return if(this == ENTRADA) "entrada" else "saida"
}
