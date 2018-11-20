package br.com.engecopi.utils

fun String?.lpad(size: Int, filler: String): String {
  var str = this ?: ""
  if(str.length > size) return str.substring(0, size)
  val buf = StringBuilder(str)
  while(buf.length < size) buf.insert(0, filler)
  
  str = buf.toString()
  return str
}

fun String?.rpad(size: Int, filler: String): String {
  val str = this ?: ""
  if(str.length > size) return str.substring(0, size)
  val buf = StringBuilder(str)
  while(buf.length < size) buf.append(filler)
  
  return buf.toString()
}

fun String?.trimNull(): String {
  return this?.trim { it <= ' ' } ?: ""
}
