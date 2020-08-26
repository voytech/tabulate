package pl.voytech.exporter.core.api.builder

interface Builder<T> {
    fun build(): T
}

abstract class InternalBuilder<T> {
    internal abstract fun build(): T
}