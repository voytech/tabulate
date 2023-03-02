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
        index.computeIfAbsent(this) { loadImageAsByteArray()}
}
