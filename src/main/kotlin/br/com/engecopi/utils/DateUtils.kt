package br.com.engecopi.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

fun LocalDateTime?.toDate(): Date? {
  if(this == null) return null
  val instant = this.atZone(ZoneId.systemDefault())?.toInstant()
  return Date.from(instant)
}

fun LocalDateTime?.toTimeStamp(): Timestamp? {
  if(this == null) return null
  val instant = this.atZone(ZoneId.systemDefault())?.toInstant()
  return Timestamp.from(instant)
}

fun LocalDate?.toDate(): Date? {
  if(this == null) return null
  val instant = this.atStartOfDay()?.atZone(ZoneId.systemDefault())?.toInstant()
  return Date.from(instant)
}

fun LocalTime?.toDate(): Date? {
  if(this == null) return null
  val date = LocalDate.now()
  val year = date.year
  val month = date.month
  val dayOfMonth = date.dayOfMonth
  val instant = this.atDate(LocalDate.of(year, month, dayOfMonth))?.atZone(ZoneId.systemDefault())?.toInstant()
  return Date.from(instant)
}

fun Date?.toLocalDateTime(): LocalDateTime? {
  if(this == null) return null
  val instant = Instant.ofEpochMilli(this.time)
  return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}

fun Date?.toLocalDate(): LocalDate? {
  if(this == null) return null
  val instant = Instant.ofEpochMilli(this.time)
  val zone = ZoneId.systemDefault()
  val zdt = instant.atZone(zone)
  return zdt.toLocalDate()
}

fun LocalDateTime?.format(): String? {
  if(this == null) return null
  val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
  return formatter.format(this)
}

fun Date?.format() : String? {
  if(this == null) return null
  val sdf = SimpleDateFormat("dd/MM/yyyy")
  return sdf.format(this)
}
