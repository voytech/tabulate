package io.github.voytech.tabulate.template.context

import java.nio.ByteBuffer

interface RollingRenderingContext: RenderingContext {
    fun getChunk(): ByteBuffer
}