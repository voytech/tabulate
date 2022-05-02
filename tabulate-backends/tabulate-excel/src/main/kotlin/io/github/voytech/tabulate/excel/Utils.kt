package io.github.voytech.tabulate.excel

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Convert [LocalDate] into [Date] used by Apache Poi
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun LocalDate.toDate(): Date = Date.from(atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

/**
 * Convert [LocalDateTime] into [Date] used by Apache Poi
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())

/**
 * Convert [String] into [Date] used by Apache Poi.
 * Uses ISO_INSTANT [DateTimeFormatter] in order to parse date.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
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