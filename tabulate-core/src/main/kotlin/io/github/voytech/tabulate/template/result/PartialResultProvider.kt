package io.github.voytech.tabulate.template.result

import io.github.voytech.tabulate.template.context.RenderingContext
import java.nio.ByteBuffer

interface PartialResultProvider<CTX: RenderingContext>: ResultProvider<CTX> {
    fun getPart(context:CTX): ByteBuffer
}