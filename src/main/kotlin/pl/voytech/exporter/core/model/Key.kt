package pl.voytech.exporter.core.model

import kotlin.reflect.KFunction

data class Key<T>(
    val id: String? = null,
    val ref: ((record: T) -> Any?)? = null
) {
    companion object {
        fun <T> field(fieldRef: ((record: T) -> Any?)) = Key(ref = fieldRef)
    }
}