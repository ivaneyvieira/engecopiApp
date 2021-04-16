package br.com.engecopi.app.forms.processaConferencia

import br.com.engecopi.saci.beans.AjusteInventario
import br.com.engecopi.saci.saci
import com.github.mvysny.karibudsl.v8.column
import com.github.mvysny.karibudsl.v8.grid
import com.github.mvysny.karibudsl.v8.panel
import com.github.mvysny.karibudsl.v8.showColumns
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.icons.VaadinIcons
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.renderers.NumberRenderer
import com.vaadin.ui.themes.ValoTheme
import org.vaadin.viritin.fields.IntegerField
import java.text.DecimalFormat

class GridPanel(private val form: ProcessaConferenciaForm) : CssLayout() {
  private val edtInv = IntegerField()
  val grid = grid(AjusteInventario::class, null, ListDataProvider(emptyList())) {
    setSizeFull()
    val binder = this.editor.binder
    val invBinding = binder.bind(edtInv, AjusteInventario::inventario.name)
    column(AjusteInventario::storeno) {
      expandRatio = 1
      caption = "Loja"
    }
    column(AjusteInventario::barcode) {
      expandRatio = 1
      caption = "Código de barras"
    }
    column(AjusteInventario::prdno) {
      expandRatio = 1
      caption = "Código"
    }
    column(AjusteInventario::descricao) {
      expandRatio = 3
      caption = "Descricao"
    }
    column(AjusteInventario::grade) {
      expandRatio = 1
      caption = "Grade"
    }
    column(AjusteInventario::inventario) {
      expandRatio = 1
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Inventario"
      editorBinding = invBinding
      this.isEditable = true
    }
    column(AjusteInventario::saldo) {
      expandRatio = 1
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Saldo"
    }
    column(AjusteInventario::qtty) {
      expandRatio = 1
      setRenderer(NumberRenderer(DecimalFormat("0")))
      setStyleGenerator { "v-align-right" }
      caption = "Quantidade"
    }

    column(AjusteInventario::cost) {
      expandRatio = 1
      setRenderer(NumberRenderer(DecimalFormat("0.0000")))
      setStyleGenerator { "v-align-right" }
      caption = "Custo Real"
    }
    column(AjusteInventario::operador) {
      expandRatio = 1
      caption = "Operador"
    }


    showColumns(AjusteInventario::barcode,
                AjusteInventario::prdno,
                AjusteInventario::descricao,
                AjusteInventario::grade,
                AjusteInventario::inventario,
                AjusteInventario::saldo,
                AjusteInventario::qtty,
                AjusteInventario::cost,
                AjusteInventario::operador)
    val colBtn = addComponentColumn { ajuste ->
      val button = Button(VaadinIcons.TRASH)
      button.addStyleName(ValoTheme.BUTTON_SMALL)
      button.description = "Apaga a linha"
      button.addClickListener {
        saci.apagaAjuste(ajuste)

        form.headerPanel.updateView(form.headerPanel.comboInventario.value)
      }
      button
    }

    colBtn.setStyleGenerator { "center" }
    colBtn.expandRatio = 1

    editor.isEnabled = true
    editor.saveCaption = "Salvar"
    editor.cancelCaption = "Cancelar"
    editor.addSaveListener { edit ->
      val ajuste = edit.bean
      saci.salvaAjuste(ajuste)

      form.headerPanel.updateView(form.headerPanel.comboInventario.value)
    }
  }

  init {
    caption = "Produtos"
    setSizeFull()
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      setSizeFull()
      content = grid
    }
  }
}