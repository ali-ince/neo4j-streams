package streams.kafka.connect.utils

import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.sink.SinkRecord
import streams.kafka.connect.sink.converters.Neo4jValueConverter
import streams.utils.JSONUtils
import streams.service.StreamsSinkEntity

fun SinkRecord.toStreamsSinkEntity(): StreamsSinkEntity = StreamsSinkEntity(
        convertData(this.key(),true),
        convertData(this.value()))

private val converter = Neo4jValueConverter()

private fun convertData(data: Any?, stringWhenFailure :Boolean = false) = when (data) {
    is Struct -> converter.convert(data)
    null -> null
    else -> JSONUtils.readValue<Any>(data, stringWhenFailure)
}