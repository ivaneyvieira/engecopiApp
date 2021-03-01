package br.com.engecopi.saci

import org.sql2o.converters.Converter

abstract class ConverterBase<T : Any> : Converter<T> {
  override fun toDatabaseParam(value: T): Any {
    return value
  }
}