package br.com.engecopi.utils

import java.io.FileInputStream
import java.util.*

class DB(banco: String) {

  private val prop = properties()

  val driver = prop.getProperty("$banco.driver") ?: ""
  val url = prop.getProperty("$banco.url") ?: ""
  val username = prop.getProperty("$banco.username") ?: ""
  val password = prop.getProperty("$banco.password") ?: ""

  companion object {
    private fun propertieFile(): String {
      return "/etc/db.conf"
    }

    private fun properties(): Properties {
      val properties = Properties()
      val configFile = FileInputStream(propertieFile())
      properties.load(configFile)
      return properties
    }
  }
}