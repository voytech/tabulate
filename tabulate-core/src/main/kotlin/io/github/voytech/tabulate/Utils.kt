package io.github.voytech.tabulate

import java.io.FileInputStream
import java.net.URL

fun String.isValidUrl(): Boolean =
    try {
        URL(this).toURI()
        true
    } catch (e: Exception) {
        false
    }

fun String.getByteArrayFromUrl(): ByteArray = URL(this).readBytes()

fun String.loadImageAsByteArray(): ByteArray =
    if (isValidUrl()) getByteArrayFromUrl() else FileInputStream(this).use { it.readBytes() }

class ImageIndex(private val index: MutableMap<String, ByteArray> = mutableMapOf()) {
    fun String.cacheImageAsByteArray(): ByteArray =
        index.computeIfAbsent(this) { loadImageAsByteArray() }
}

interface ResettableIterator<T> {
    fun reset()
    fun currentOrNull(): T?
    fun current(): T
    fun nextOrNull(): T?
    fun currentIndex(): Int
}

interface ResettableComplexIterator<T, E> where E : Enum<E>, T : Any {
    fun reset(enum: E)
    fun currentOrNull(enum: E): T?
    fun current(enum: E): T
    fun next(enum: E): T
    fun nextOrNull(enum: E): T?
    fun currentIndex(enum: E): Int
}

class DefaultResettableIterator<T>(
    private val list: MutableList<T>,
) : AbstractIterator<T>(), ResettableIterator<T> {

    private var index: Int = -1

    override fun computeNext() {
        if (index >= list.size || list.isEmpty()) {
            done()
        } else {
            setNext(list[++index])
        }
    }

    override fun reset() {
        index = -1
    }

    override fun currentOrNull(): T? = list[index]

    override fun current(): T = currentOrNull()!!

    override fun nextOrNull(): T? = if (hasNext()) next() else null

    override fun currentIndex(): Int = index
}

data class DefaultComplexIterator<T, E>(
    private val list: MutableList<T>,
) : ResettableComplexIterator<T, E> where E : Enum<E>, T : Any {

    private val iterations: MutableMap<E, DefaultResettableIterator<T>> = mutableMapOf()

    private fun <R> usingIteration(enum: E, block: DefaultResettableIterator<T>.() -> R?): R? {
        iterations.computeIfAbsent(enum) { DefaultResettableIterator(list) }
        return iterations[enum]?.run(block)
    }

    override fun next(enum: E): T =
        usingIteration(enum) { next() } ?: error("No next element for iteration: $enum")

    override fun reset(enum: E) {
        usingIteration(enum) {
            reset()
        }
    }

    override fun currentOrNull(enum: E): T? = usingIteration(enum) {
        currentOrNull()
    }

    override fun current(enum: E): T =
        usingIteration(enum) { current() } ?: error("No current element for iteration: $enum")

    override fun nextOrNull(enum: E): T? = usingIteration(enum) { nextOrNull() }

    override fun currentIndex(enum: E): Int =
        usingIteration(enum) { currentIndex() } ?: error("No current element for iteration: $enum")

}