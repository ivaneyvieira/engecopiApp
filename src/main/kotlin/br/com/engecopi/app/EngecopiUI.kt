package br.com.engecopi.app

import br.com.engecopi.saci.QuerySaci
import com.github.mvysny.karibudsl.v8.autoViewProvider
import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewDisplay
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Component
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout

//@Theme("mytheme")
@Theme("valo")
@Title("Engecopi App")
class EngecopiUI : UI() {
  private val content = Content()

  override fun init(request: VaadinRequest?) {
    page.setTitle("Engecopi App: " + QuerySaci.ipServer)
    setContent(content)
    navigator = Navigator(this, content as ViewDisplay)
    navigator.addProvider(autoViewProvider)
    setErrorHandler { e ->
      e.throwable.printStackTrace()
    }
  }
}

private class Content : VerticalLayout(), ViewDisplay {
  init {
    setSizeFull()
    setMargin(false)
  }

  override fun showView(view: View?) {
    removeAllComponents()
    addComponent(view as Component)
  }
}
