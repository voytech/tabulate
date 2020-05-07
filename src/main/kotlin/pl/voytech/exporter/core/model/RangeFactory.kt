package pl.voytech.exporter.core.model

fun infinite(start: Long): LongRange = LongRange(start, Long.MAX_VALUE)
fun infinite(): LongRange = LongRange(0L, Long.MAX_VALUE)
