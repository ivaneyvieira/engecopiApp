package br.com.engecopi.app.forms.ajustaEstoque

import br.com.engecopi.app.model.Base
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.app.model.TipoMov.SAIDA
import br.com.engecopi.saci.saci
import com.github.mvysny.karibudsl.v8.alignment
import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.checkBox
import com.github.mvysny.karibudsl.v8.comboBox
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
import com.vaadin.ui.Notification.Type.ERROR_MESSAGE
import com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.RadioButtonGroup
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme
import de.steinwedel.messagebox.MessageBox

class HeaderPanel(private val ajustaEstoqueForm: AjustaEstoqueForm): VerticalLayout() {
  private lateinit var fornecedorNF: TextField
  private lateinit var incluirNoras: CheckBox
  private lateinit var edtTipos: TextField
  private lateinit var edtFornecedores: TextField
  private lateinit var codigo: TextField
  private lateinit var tipoMov: RadioButtonGroup<TipoMov>
  private lateinit var loja: ComboBox<Loja>
  
  init {
    this.w = 100.perc
    this.isMargin = false
    
    this.horizontalLayout {
      this.w = 100.perc
      loja = comboBox("Loja") {
        isEmptySelectionAllowed = false
        isTextInputAllowed = false
        val list = Loja.values().toList()
        setItems(list)
        value = list.firstOrNull()
        setItemCaptionGenerator {it.numero.toString() + " - " + it.descricao}
        isExpanded = false
      }
      tipoMov = radioButtonGroup("Tipo") {
        styleName = ValoTheme.OPTIONGROUP_HORIZONTAL
        
        setItems(TipoMov.values().toList())
        setItemIconGenerator {it.icon}
        value = SAIDA
        isExpanded = true
      }
    }
    this.horizontalLayout {
      this.w = 100.perc
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
        onLeftClick(::clickExecuta)
      }
      button("Desfazer") {
        this.alignment = Alignment.BOTTOM_RIGHT
        this.isExpanded = false
        onLeftClick(::clickDesfazer)
      }
    }
  }
  
  private fun clickDesfazer(clickEvent: Button.ClickEvent) {
    val edtLoja: ComboBox<Loja>
    val edtNota: TextField
    val edtTipoMov: RadioButtonGroup<TipoMov>
    val form = VerticalLayout().apply {
      edtLoja = comboBox("Loja") {
        isEmptySelectionAllowed = false
        isTextInputAllowed = false
        val list = Loja.values().toList()
        setItems(list)
        value = loja.value
        setItemCaptionGenerator {it.numero.toString() + " - " + it.descricao}
        isExpanded = false
      }
      edtNota = textField("Nota")
      edtTipoMov = radioButtonGroup("Tipo") {
        styleName = ValoTheme.OPTIONGROUP_HORIZONTAL
        
        setItems(TipoMov.values().toList())
        setItemIconGenerator {it.icon}
        value = tipoMov.value
        isExpanded = true
      }
    }
    MessageBox.create().withCaption("Desfazer").withMessage(form).withYesButton({
                                                                                  confirmaDesfazer(edtLoja.value?.numero,
                                                                                                   edtNota.value,
                                                                                                   edtTipoMov.value)
                                                                                })
      .withNoButton({println("No button was pressed.")}).open()
  }
  
  private fun confirmaDesfazer(numLoja: Int?, nota: String?, tipo: TipoMov?) {
    numLoja ?: return
    val numNota = nota?.toIntOrNull() ?: return
    tipo ?: return
    
    try {
      val valido = when(tipo) {
        SAIDA   -> saci.validarNfSaida(numLoja, numNota)
        ENTRADA -> saci.validarNfEntrada(numLoja, numNota)
      }
      
      if(valido) {
        when(tipo) {
          SAIDA   -> saci.desfazerSaida(numLoja, numNota)
          ENTRADA -> saci.desfazerEntrada(numLoja, numNota)
        }
        show("Movimentacao referente a nota: $numNota da loja: $numLoja foi desfeita com sucesso!", HUMANIZED_MESSAGE)
      }
      else {
        show("Informe uma nota válida!", ERROR_MESSAGE)
      }
    } catch(e: Exception) {
      val msgErro = "Não foi possível Listar os produtos! Erro:$e"
      show(msgErro, ERROR_MESSAGE)
    }
  }
  
  private fun clickExecuta(clickEvent: Button.ClickEvent) {
    MessageBox.createQuestion().withCaption("Alerta").withMessage("Tem Certeza?").withYesButton(::confirmaExecuta)
      .withNoButton({println("No button was pressed.")}).open()
  }
  
  private fun confirmaExecuta() {
    try {
      val nota = saci.executar(baseDados())
      show("Nota de movimentação gerada: $nota", HUMANIZED_MESSAGE)
    } catch(e: Exception) {
      val msgErro = "Não foi possível Listar os produtos! Erro:$e"
      show(msgErro, ERROR_MESSAGE)
    }
  }
  
  private fun clickBusca(clickEvent: Button.ClickEvent) {
    try {
      val produtos = saci.buscaProdutos(baseDados())
      ajustaEstoqueForm.setProdutos(produtos)
    } catch(e: Exception) {
      val msgErro = "Não foi possível Listar os produtos! Erro:$e"
      show(msgErro, ERROR_MESSAGE)
    }
  }
  
  fun baseDados() = Base(
    lojaDestino = loja.value?.numero ?: 0,
    operacao = tipoMov.value.operacao(),
    codprd = codigo.value ?: "",
    fornecedores = edtFornecedores.value ?: "",
    tipos = edtTipos.value ?: "",
    incluiNfFornecedor = incluirNoras.value ?: false,
    fornecedorNf = fornecedorNF.value ?: "",
                        )
}

private fun TipoMov.operacao(): String {
  return if(this == ENTRADA) "entrada" else "saida"
}
