package br.com.engecopi.app.forms.processaConferencia

import br.com.engecopi.saci.beans.AjusteInventario
import br.com.engecopi.saci.beans.Inventario
import br.com.engecopi.saci.saci
import com.github.mvysny.karibudsl.v8.cssLayout
import com.github.mvysny.karibudsl.v8.expandRatio
import com.github.mvysny.karibudsl.v8.getAll
import com.github.mvysny.karibudsl.v8.horizontalLayout
import com.github.mvysny.karibudsl.v8.isMargin
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.ComboBox
import com.vaadin.ui.DateField
import com.vaadin.ui.FormLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme
import de.steinwedel.messagebox.ButtonOption
import de.steinwedel.messagebox.MessageBox
import org.vaadin.viritin.fields.IntegerField
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HeaderPanel(val form: ProcessaConferenciaForm): VerticalLayout() {
  private val btnW = 120F
  fun inventarios() = saci.inventarios()
  
  val comboInventario = ComboBox<Inventario>("Inventário").apply {
    this.isEmptySelectionAllowed = false
    this.isTextInputAllowed = false
    setItemCaptionGenerator {inv -> "${inv.numero} - ${inv.dataFormat()}"}
    addValueChangeListener {
      val value = it.value
      updateView(value)
    }
  }
  private val nfEntrada = TextField("NF Entrada").apply {
    setWidth(150F, PIXELS)
    isReadOnly = true
  }
  private val nfSaida = TextField("NF Saída").apply {
    setWidth(150F, PIXELS)
    isReadOnly = true
  }
  private val numLoja = TextField("Loja").apply {
    setWidth(75F, PIXELS)
    isReadOnly = true
  }
  private val btnProcessa = Button("Processa").apply {
    setWidth(btnW, PIXELS)
    addClickListener {
      comboInventario.value?.let {inv ->
        if(inv.processado())
          MessageBox.createWarning()
            .withCaption("Aviso")
            .withMessage("O ajuste já está processado!")
            .open()
        else
          inv.numero?.let {numero ->
            saci.processaAjuste(numero)
            updateCombo(comboInventario)
            updateView(inv)
          }
      }
    }
  }
  private val btnDesfaz = Button("Desfaz").apply {
    setWidth(btnW, PIXELS)
    addClickListener {
      comboInventario.value?.let {inv ->
        if(!inv.processado())
          MessageBox.createWarning()
            .withCaption("Aviso")
            .withMessage("O ajuste não já está processado!")
            .open()
        else
          inv.numero?.let {numero ->
            saci.defazAjuste(numero)
            updateView(inv)
          }
      }
    }
  }
  private val btnNovoAjuste = Button("Novo Ajuste").apply {
    setWidth(btnW, PIXELS)
    addClickListener {
      saci.novoAjuste()
      updateCombo(comboInventario)
      
      comboInventario.value =
        comboInventario.dataProvider.getAll()
          .firstOrNull()
    }
  }
  private val btnAtualizaAjuste = Button("Atualiza ").apply {
    setWidth(btnW, PIXELS)
    addClickListener {
      comboInventario.value?.let {inv ->
        updateView(inv)
        updateCombo(comboInventario)
      }
    }
  }
  private val btnNovoProduto = Button("Adicionar").apply {
    setWidth(btnW, PIXELS)
    addClickListener {
      val cmbGrade = ComboBox<String>("Grade").apply {
        isTextInputAllowed = false
      }
      val codigo = TextField("Código").apply {
        addValueChangeListener {e ->
          if(e.isUserOriginated) {
            val value = e.value
            val grades = saci.findGrades(value)
            if(grades.isEmpty()) {
              cmbGrade.setItems()
              cmbGrade.emptySelectionCaption = "Sem grades"
              cmbGrade.isEmptySelectionAllowed = true
            }
            else {
              cmbGrade.setItems(grades)
              cmbGrade.isEmptySelectionAllowed = false
              cmbGrade.value = grades.firstOrNull()
            }
          }
        }
      }
      val quant = IntegerField("Quantidade").apply {
        value = 1
        addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT)
      }
      val form = FormLayout(codigo, cmbGrade, quant)
      MessageBox.create()
        .withCaption("Adicionar produto")
        .withMessage(form)
        .withNoButton(ButtonOption.caption("Cancelar"))
        .withYesButton({
          salvarNovo(codigo = codigo.value,
                     grade = cmbGrade.value ?: "",
                     qtty = quant.value)
        },
                       ButtonOption.caption("Salvar"),
                       ButtonOption.focus())
        .withWidth("380px")
        .open()
    }
  }
  
  fun salvarNovo(codigo: String, grade: String, qtty: Int) {
    comboInventario.value?.let {inv ->
      if(!inv.processado()) {
        val loja: Int = inv.storeno ?: 0
        val nota = inv.numero?.toInt() ?: 0
        val dataAjuste = inv.date ?: 0
        if(nota > 0 && dataAjuste > 0 && loja > 0) {
          saci.addProdutoAjuste(loja, codigo, grade, nota, qtty, dataAjuste)
          updateView(inv)
          updateCombo(comboInventario)
        }
      }
    }
  }
  
  private val dataInicial = DateField("Data Inicial").apply {
    value =
      LocalDate.now()
        .minusDays(60)
    dateFormat = "dd/MM/yyyy"
    addValueChangeListener {updateCombo(comboInventario)}
  }
  private val dataFinal = DateField("Data Final").apply {
    value = LocalDate.now()
    dateFormat = "dd/MM/yyyy"
    addValueChangeListener {updateCombo(comboInventario)}
  }
  private val comboTipo = ComboBox<Int>("Status").apply {
    this.isEmptySelectionAllowed = false
    this.isTextInputAllowed = false
    setItems(1, 2, 3)
    setItemCaptionGenerator {num ->
      when(num) {
        1 -> "Todos"
        2 -> "Processado"
        3 -> "Não Processado"
        else -> ""
      }
    }
    value = 3
    addValueChangeListener {updateCombo(comboInventario)}
  }
  
  private fun dataSaci(data: LocalDate = LocalDate.now()): Int {
    val sdf = DateTimeFormatter.ofPattern("yyyyMMdd")
    val strDate = sdf.format(data)
    return strDate.toInt()
  }
  
  private fun updateCombo(combo: ComboBox<Inventario>) {
    val inventarios = inventarios().filter {inv ->
      val dateInv =
        inv.date
        ?: 0
      if(dateInv < dataSaci(dataInicial.value))
        false
      else {
        if(dateInv > dataSaci(dataFinal.value))
          false
        else {
          when(comboTipo.value) {
            1 -> true
            2 -> inv.processado()
            3 -> !inv.processado()
            else -> false
          }
        }
      }
    }
    val inv = combo.value
    val value =
      inventarios.find {it.numero == inv?.numero}
      ?: inventarios.firstOrNull()
    combo.setItems(inventarios)
    value?.let {combo.setSelectedItem(it)}
  }
  
  init {
    isMargin = false
    updateCombo(comboInventario)
    setWidth("100%")
    nfEntrada.isReadOnly = true
    nfSaida.isReadOnly = true
    numLoja.isReadOnly = true
    horizontalLayout {
      defaultComponentAlignment = Alignment.BOTTOM_LEFT
      addComponents(dataInicial, dataFinal, comboTipo)
    }
    horizontalLayout {
      setWidth("100%")
      defaultComponentAlignment = Alignment.BOTTOM_LEFT
      
      addComponents(comboInventario, numLoja, nfEntrada, nfSaida)
      cssLayout {
        expandRatio = 2.0F
      }
      addComponents(btnNovoAjuste, btnAtualizaAjuste, btnProcessa, btnDesfaz, btnNovoProduto)
    }
  }
  
  fun updateView(inv: Inventario) {
    val ajustes = ajusteInventario(inv)
    nfEntrada.value = ""
    nfSaida.value = ""
    numLoja.value = ""
    ajustes.firstOrNull()
      ?.let {ajuste ->
        nfEntrada.value = ajuste.nfEntrada + if(ajuste.nfEntrada.isNullOrBlank()) "" else "/66"
        nfSaida.value = ajuste.nfSaida + if(ajuste.nfSaida.isNullOrBlank()) "" else "/66"
        numLoja.value = "${ajuste.storeno}"
      }
    val list: List<AjusteInventario> = if(comboInventario.value.processado())
      emptyList()
    else
      ajustes
    form.gridPanel.grid.dataProvider = ListDataProvider<AjusteInventario>(list)
    btnNovoProduto.isEnabled = inv.processado() == false
    btnProcessa.isEnabled = inv.processado() == false
    btnDesfaz.isEnabled = inv.processado() == true
  }
  
  private fun ajusteInventario(value: Inventario): List<AjusteInventario> {
    return value.numero?.let {numero ->
      saci.ajustesInventario(numero)
    } ?: emptyList()
  }
}