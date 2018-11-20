package br.com.engecopi.app

import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinServlet
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.servlet.annotation.WebServlet

@WebListener
class Bootstrap : ServletContextListener {
  override fun contextInitialized(sce: ServletContextEvent?) {
    log.info("Starting up")
    
    log.info("Initializing VaadinOnKotlin")
  }
  
  override fun contextDestroyed(sce: ServletContextEvent?) {
  
  }
  
  companion object {
    private val log = LoggerFactory.getLogger(Bootstrap::class.java)
    
    init {
      // let java.util.logging log to slf4j
      SLF4JBridgeHandler.removeHandlersForRootLogger()
      SLF4JBridgeHandler.install()
    }
  }
}

@WebServlet(urlPatterns = arrayOf("/*"), name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = EngecopiUI::class, productionMode = false)
class MyUIServlet : VaadinServlet()

//@ApplicationPath("/rest")
//class ApplicationConfig : Application()
