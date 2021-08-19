package io.github.voytech.tabulate.template.result

import io.github.voytech.tabulate.template.context.RenderingContext
import java.nio.ByteBuffer

/**
 * Having rendering context, extract [ByteBuffer] representing rendered part (row).
 * This [ByteBuffer] is then directly appended to output.
 */
interface PartialResultProvider<CTX: RenderingContext>: ResultProvider<CTX> {
    fun getPart(context:CTX): ByteBuffer
}