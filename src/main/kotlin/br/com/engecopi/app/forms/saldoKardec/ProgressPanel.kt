package br.com.engecopi.app.forms.saldoKardec

import com.github.vok.karibudsl.*
import com.vaadin.ui.CssLayout
import com.vaadin.ui.Label
import com.vaadin.ui.ProgressBar
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme

class ProgressPanel : CssLayout() {
  private lateinit var progressBar: ProgressBar
  private lateinit var progressCaption: Label

  init {
    setWidth("100%")
    styleName = ValoTheme.LAYOUT_WELL
    panel {
      verticalLayout {
        setWidth("100%")
        isMargin = true
        progressCaption = label("Progresso")

        progressBar = progressBar {
          setWidth("100%")
          value = 0f
          reset()
        }
      }
    }
  }

  fun update(caption: String, value: Float) {
    isVisible = caption != ""
    progressBar.value = value
    progressCaption.value  = caption
  }
}