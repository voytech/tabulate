package io.github.voytech.tabulate

import java.net.URL

fun String.isValidUrl(): Boolean =
    try {
        URL(this).toURI()
        true
    } catch (e: Exception) {
        false
    }

fun String.getByteArrayFromUrl(): ByteArray = URL(this).readBytes()