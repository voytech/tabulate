package io.github.voytech.tabulate

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.round

fun String.ellipsis(maxLen: Int = 20): String =
    if (length > maxLen) "${substring(0,maxLen)}..." else this

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

sealed class Either<A, B> {
    class Left<A, B>(val value: A) : Either<A, B>()
    class Right<A, B>(val value: B) : Either<A, B>()
}

fun Float.round3(): Float = (round(this * 1000)) / 1000

fun Float.round1(): Float = (round(this * 10)) / 10

fun Float.round(precision: Int): Float = 10F.pow(precision).let { (round(this * it)) / it }