package io.github.voytech.tabulate.template.context

import java.nio.ByteBuffer

interface ReactiveRenderingContext: RenderingContext {
    fun getBuffer(): ByteBuffer
}