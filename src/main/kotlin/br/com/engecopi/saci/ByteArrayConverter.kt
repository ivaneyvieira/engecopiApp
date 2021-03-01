package br.com.engecopi.saci

import org.sql2o.converters.ConverterException
import org.sql2o.tools.IOUtils
import java.io.IOException
import java.io.InputStream
import java.sql.Blob
import java.sql.SQLException

class ByteArrayConverter : ConverterBase<ByteArray>() {
  @Throws(ConverterException::class)
  override fun convert(value: Any): ByteArray? {
    if (value is Blob) {
      var stream: InputStream? = null
      return try {
        try {
          stream = value.binaryStream
          IOUtils.toByteArray(stream)
        } finally {
          if (stream != null) {
            try {
              stream.close()
            } catch (ignore: Throwable) { // ignore stream.close errors
            }
          }
          try {
            value.free()
          } catch (ignore: Throwable) { // ignore blob.free errors
          }
        }
      } catch (e: SQLException) {
        throw ConverterException("Error converting Blob to byte[]", e)
      } catch (e: IOException) {
        throw ConverterException("Error converting Blob to byte[]", e)
      }
    }
    if (value is ByteArray) {
      return value
    }
    throw RuntimeException("could not convert " + value.javaClass.name + " to byte[]")
  }
}
