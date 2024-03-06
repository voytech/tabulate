package io.github.voytech.tabulate

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.pow
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
        val nextIndex = index + 1
        if (nextIndex >= view.size) {
            done()
        } else {
            setNext(view[nextIndex])
            index = nextIndex
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

    private val collection: LinkedHashSet<T> = LinkedHashSet()

    private val iterations: MutableMap<E, AdjustingIterator<T>> = mutableMapOf()

    private fun <R> usingCategory(enum: E, block: AdjustingIterator<T>.() -> R?): R? {
        iterations.computeIfAbsent(enum) { AdjustingIterator(collection) }
        return iterations[enum]?.run(block)
    }

    fun find(pred: (T) -> Boolean): T? = collection.find(pred)

    fun insert(enum: E, elem: T) {
        usingCategory(enum) {
            val index = currentIndex()
            val list = collection.toMutableList().also { it.add(index + 1, elem) }
            collection.clear()
            collection.addAll(list)
            iterations.values.forEach { it.rebuild() }
        }
    }

    fun add(elem: T) {
        collection.add(elem)
        iterations.values.forEach { it.rebuild() }
    }

    override fun next(enum: E): T =
        usingCategory(enum) { next() } ?: error("No next element for iteration: $enum")

    override fun reset(enum: E) {
        usingCategory(enum) {
            restart()
        }
    }

    override fun currentOrNull(enum: E): T? = usingCategory(enum) {
        currentOrNull()
    }

    override fun current(enum: E): T =
        usingCategory(enum) { current() } ?: error("No current element for iteration: $enum")

    override fun nextOrNull(enum: E): T? = usingCategory(enum) { nextOrNull() }

    override fun currentIndex(enum: E): Int =
        usingCategory(enum) { currentIndex() } ?: -1

    override fun E.isAfter(other: E): Boolean = currentIndex(this) <= currentIndex(other)
    override fun size(): Int = collection.size

    override fun clear() {
        collection.clear()
        iterations.values.forEach { it.rebuild() }
    }

    fun lastOrNull(): T? = collection.lastOrNull()

}

operator fun <T, E> MultiIterationSet<T, E>.plusAssign(element: T) where E : Enum<E>, T : Any {
    add(element)
}

sealed class Either<A, B> {
    class Left<A, B>(val value: A) : Either<A, B>()
    class Right<A, B>(val value: B) : Either<A, B>()
}

fun Float.round3(): Float = (round(this * 1000)) / 1000

fun Float.round(precision: Int): Float = 10F.pow(precision).let { (round(this * it)) / it }