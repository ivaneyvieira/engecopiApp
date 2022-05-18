package br.com.engecopi.saci

import org.sql2o.converters.Converter
import org.sql2o.converters.ConverterException
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneOffset

class LocalDateConverter : Converter<LocalDate?> {
  @Throws(ConverterException::class)
  override fun convert(value: Any?): LocalDate? {
    if (value !is Date) return null
    return value.toLocalDate()
  }

  override fun toDatabaseParam(value: LocalDate?): Any? {
    value ?: return null
    return Date(value.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
  }
}