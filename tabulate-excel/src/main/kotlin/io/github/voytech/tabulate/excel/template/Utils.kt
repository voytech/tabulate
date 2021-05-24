package io.github.voytech.tabulate.excel.template

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

fun LocalDateTime.toDate(): Date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

fun String.toDate(): Date = Date.from(Instant.parse(this))

object Utils {
    fun toDate(value: Any): Date {
        return when (value) {
            is LocalDate -> value.toDate()
            is LocalDateTime -> value.toDate()
            is String -> value.toDate()
            is Date -> value
            else -> throw IllegalStateException("Could not parse Date.")
        }
    }
}