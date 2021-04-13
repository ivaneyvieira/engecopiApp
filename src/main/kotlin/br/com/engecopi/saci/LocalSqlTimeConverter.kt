package br.com.engecopi.saci

import org.sql2o.converters.Converter
import org.sql2o.converters.ConverterException
import java.sql.Time
import java.time.LocalTime

class LocalSqlTimeConverter : Converter<LocalTime?> {
  @Throws(ConverterException::class) override fun convert(value: Any?): LocalTime? {
    if (value !is Time) return null
    return value.toLocalTime()
  }

  override fun toDatabaseParam(value: LocalTime?): Any? {
    value ?: return null
    return Time.valueOf(value)
  }
}