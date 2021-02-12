package br.com.engecopi.app

import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinServlet
import java.util.*
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.servlet.annotation.WebServlet

@WebListener
class Bootstrap: ServletContextListener {
  override fun contextInitialized(sce: ServletContextEvent?) {
    Locale.setDefault(Locale("pt", "BR"))
  }
  
  override fun contextDestroyed(sce: ServletContextEvent?) {
  }
  
  companion object { //private val log = LoggerFactory.getLogger(Bootstrap::class.java)
    init { // let java.util.logging log to slf4j
      //SLF4JBridgeHandler.removeHandlersForRootLogger()
      // SLF4JBridgeHandler.install()
    }
  }
}

@WebServlet(urlPatterns = arrayOf("/*"), name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = EngecopiUI::class, productionMode = false)
class MyUIServlet: VaadinServlet() //@ApplicationPath("/rest")
//class ApplicationConfig : Application()
