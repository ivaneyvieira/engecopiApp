package br.com.engecopi.app.forms.ajustaEstoquePerda

import br.com.engecopi.app.model.Base
import br.com.engecopi.app.model.Loja
import br.com.engecopi.app.model.TipoMov
import br.com.engecopi.app.model.TipoMov.ENTRADA
import br.com.engecopi.app.model.TipoMov.SAIDA
import br.com.engecopi.app.model.TipoNota
import br.com.engecopi.app.model.TipoNota.PERDA
import br.com.engecopi.saci.saci
import br.com.engecopi.utils.rpad
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.ui.*
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Notification.Type.ERROR_MESSAGE
import com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE
import com.vaadin.ui.Notification.show
import com.vaadin.ui.themes.ValoTheme
import de.steinwedel.messagebox.MessageBox
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HeaderPanel(private val ajustaEstoqueFormPerda: AjustaEstoqueFormPerda) : VerticalLayout() {
  private lateinit var edtTipos: TextField
  private lateinit var edtFornecedores: TextField
  private lateinit var codigo: TextField
  private lateinit var tipoMov: RadioButtonGroup<TipoMov>
  private lateinit var loja: ComboBox<Loja>
  private lateinit var mesAno: TextField

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
        setItemCaptionGenerator { it.numero.toString() + " - " + it.descricao }
        isExpanded = false
      }
      tipoMov = radioButtonGroup("Tipo") {
        styleName = ValoTheme.OPTIONGROUP_HORIZONTAL

        setItems(TipoMov.values().toList())
        setItemIconGenerator { it.icon }
        value = SAIDA
        isExpanded = false
      }
      mesAno = textField("Mes/Ano") {
        val formatter = DateTimeFormatter.ofPattern("MM/yyyy")
        value = LocalDate.now().format(formatter)
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

  private fun clickDesfazer(clickEvent: ClickEvent) {
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
        setItemCaptionGenerator { it.numero.toString() + " - " + it.descricao }
        isExpanded = false
      }
      edtNota = textField("Nota")
      edtTipoMov = radioButtonGroup("Tipo") {
        styleName = ValoTheme.OPTIONGROUP_HORIZONTAL

        setItems(TipoMov.values().toList())
        setItemIconGenerator { it.icon }
        value = tipoMov.value
        isExpanded = true
      }
    }
    MessageBox.create().withCaption("Desfazer").withMessage(form).withYesButton({
      confirmaDesfazer(edtLoja.value, edtNota.value, edtTipoMov.value, PERDA)
    }).withNoButton({ println("No button was pressed.") }).open()
  }

  private fun confirmaDesfazer(loja: Loja?, nota: String?, tipo: TipoMov?, tipoNota: TipoNota?) {
    loja ?: return
    val numNota = nota?.toIntOrNull() ?: return
    tipoNota ?: return
    tipo ?: return

    try {
      val valido = when (tipo) {
        SAIDA -> saci.validarNfSaida(loja, numNota, tipoNota)
        ENTRADA -> saci.validarNfEntrada(loja, numNota, tipoNota)
      }

      if (valido) {
        when (tipo) {
          SAIDA -> saci.desfazerSaida(loja, numNota)
          ENTRADA -> saci.desfazerEntrada(loja, numNota)
        }
        show(
          "Movimentacao referente a nota: $numNota da loja: ${loja.numero} foi desfeita com sucesso!", HUMANIZED_MESSAGE
            )
      }
      else {
        show("Informe uma nota válida!", ERROR_MESSAGE)
      }
    } catch (e: Exception) {
      val msgErro = "Não foi possível Listar os produtos! Erro:$e"
      e.printStackTrace()
      show(msgErro, ERROR_MESSAGE)
    }
  }

  private fun clickExecuta(clickEvent: ClickEvent) {
    MessageBox.createQuestion()
            .withCaption("Alerta")
            .withMessage("Tem Certeza?")
            .withYesButton(::confirmaExecuta)
            .withNoButton({ println("No button was pressed.") })
            .open()
  }

  private fun confirmaExecuta() {
    try {
      val transacao = saci.executarPerda(baseDados())
      show("Transação de movimentação gerada: $transacao", HUMANIZED_MESSAGE)
    } catch (e: Exception) {
      val msgErro = "Não foi possível Listar os produtos! Erro:$e"
      e.printStackTrace()
      show(msgErro, ERROR_MESSAGE)
    }
  }

  private fun clickBusca(clickEvent: ClickEvent) {
    try {
      val produtos = saci.buscaProdutos(baseDados())
      ajustaEstoqueFormPerda.setProdutos(produtos)
    } catch (e: Exception) {
      val msgErro = "Não foi possível Listar os produtos! Erro:$e"
      e.printStackTrace()
      show(msgErro, ERROR_MESSAGE)
    }
  }

  fun baseDados() = Base(
    lojaDestino = loja.value?.numero ?: 0,
    operacao = tipoMov.value,
    codprd = codigo.value?.trim() ?: "",
    fornecedores = edtFornecedores.value ?: "",
    tipos = edtTipos.value ?: "",
    mesAno = mesAno.value?.numMesAno() ?: 0
                        )
}

private fun String.numMesAno(): Int {
  val num = this.rpad(10, "0")
  val strNum = num.substring(3, 7) + num.substring(0, 2)
  return strNum.toIntOrNull() ?: 0
}

