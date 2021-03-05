package br.com.engecopi.utils

import java.text.DecimalFormat

private val formatNumber = DecimalFormat("#,##0.00")
private val formatInteger = DecimalFormat("#,##0")

fun Double?.format(): String {
  this ?: return ""
  return formatNumber.format(this)
}

fun Int?.format(): String {
  this ?: return ""
  return formatInteger.format(this)
}

fun String?.lpad(size: Int, filler: String): String {
  var str = this ?: ""
  if (str.length > size) return str.substring(0, size)
  val buf = StringBuilder(str)
  while (buf.length < size) buf.insert(0, filler)

  str = buf.toString()
  return str
}

fun String?.rpad(size: Int, filler: String): String {
  val str = this ?: ""
  if (str.length > size) return str.substring(0, size)
  val buf = StringBuilder(str)
  while (buf.length < size) buf.append(filler)

  return buf.toString()
}

fun String?.trimNull(): String {
  return this?.trim { it <= ' ' } ?: ""
}
