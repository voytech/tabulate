package io.github.voytech.tabulate

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.round

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

fun String.loadBufferedImage(): BufferedImage =
    if (isValidUrl()) ImageIO.read(URL(this)) else ImageIO.read(File(this))

class ImageIndex(private val index: MutableMap<String, ByteArray> = mutableMapOf()) {
    fun String.cacheImageAsByteArray(): ByteArray =
        index.computeIfAbsent(this) { loadImageAsByteArray() }
}

interface CompositeIterator<T, E> where E : Enum<E>, T : Any {
    fun reset(enum: E)
    fun currentOrNull(enum: E): T?
    fun current(enum: E): T
    fun next(enum: E): T
    fun nextOrNull(enum: E): T?
    fun currentIndex(enum: E): Int
    infix fun E.isAfter(other: E): Boolean

    fun size(): Int

    fun clear()
}

class AdjustingIterator<T>(
    private val collection: Iterable<T>,
) : AbstractIterator<T>() {

    private var index: Int = -1

    private var view: List<T> = collection.toList()

    override fun computeNext() {
        require(view.isNotEmpty())
        if (index >= view.size) {
            done()
        } else {
            setNext(view[++index])
        }
    }

    fun restart(index: Int = -1) {
        this.index = index
        rebuild()
    }

    fun rebuild() {
        view = collection.toList()
    }

    fun currentOrNull(): T? = view.getOrNull(index)

    fun current(): T = currentOrNull()!!

    fun nextOrNull(): T? = if (view.isNotEmpty() && hasNext()) next() else null

    fun currentIndex(): Int = index
}

class MultiIterationSet<T, E> : CompositeIterator<T, E> where E : Enum<E>, T : Any {

    private val collection: MutableCollection<T> = LinkedHashSet()

    private val iterations: MutableMap<E, AdjustingIterator<T>> = mutableMapOf()

    private fun <R> usingIteration(enum: E, block: AdjustingIterator<T>.() -> R?): R? {
        iterations.computeIfAbsent(enum) { AdjustingIterator(collection) }
        return iterations[enum]?.run(block)
    }

    fun add(elem: T) {
        collection.add(elem)
        iterations.values.forEach { it.rebuild() }
    }

    override fun next(enum: E): T =
        usingIteration(enum) { next() } ?: error("No next element for iteration: $enum")

    override fun reset(enum: E) {
        usingIteration(enum) {
            restart()
        }
    }

    override fun currentOrNull(enum: E): T? = usingIteration(enum) {
        currentOrNull()
    }

    override fun current(enum: E): T =
        usingIteration(enum) { current() } ?: error("No current element for iteration: $enum")

    override fun nextOrNull(enum: E): T? = usingIteration(enum) { nextOrNull() }

    override fun currentIndex(enum: E): Int =
        usingIteration(enum) { currentIndex() } ?: -1

    override fun E.isAfter(other: E): Boolean = currentIndex(this) <= currentIndex(other)
    override fun size(): Int = collection.size

    override fun clear() {
        collection.clear()
        iterations.values.forEach { it.rebuild() }
    }

    fun lastOrNull(): T? = collection.lastOrNull()

}

operator fun <T,E> MultiIterationSet<T,E>.plusAssign(element: T)  where E : Enum<E>, T : Any {
    add(element)
}

sealed class Either<A, B> {
    class Left<A, B>(val value: A) : Either<A, B>()
    class Right<A, B>(val value: B) : Either<A, B>()
}

fun Float.round3(): Float = (round(this*1000))/1000