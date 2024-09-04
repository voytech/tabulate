package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.overflow.Overflow
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.core.operation.RenderingClipped
import io.github.voytech.tabulate.core.operation.RenderingResult
import io.github.voytech.tabulate.core.operation.RenderingSkipped

abstract class DirectlyRenderableModel<R : RenderableEntity<SimpleLayout>> : ModelWithAttributes() {

    override fun doExport(api: ExportApi): Unit = api {
        val renderable = asRenderable()
        render(renderable).let { result ->
            traceSection("Rendered: $renderable, ${result.status}")
            iterations { catchOverflow(result) }
        }
    }

    override fun takeMeasures(api: ExportApi): Unit = api {
        val renderable = asRenderable()
        measure(renderable).let { result ->
            traceSection("Measured: $renderable, ${result.status}")
            iterations { catchOverflow(result) }
        }
    }

    private fun ExportApi.catchOverflow(result: RenderingResult) = iterations {
        val overflow = getOverflowHandlingStrategy(result)
        when (result.status) {
            is RenderingClipped -> {
                when (overflow) {
                    Overflow.STOP -> stop()
                    Overflow.FINISH -> finish()
                    else -> {}
                }
            }

            is RenderingSkipped -> {
                when (overflow) {
                    Overflow.RETRY, null -> retryIteration()
                    Overflow.STOP -> stop()
                    else -> {}
                }
            }
            else -> {}
        }
    }

    protected abstract fun ExportApi.asRenderable(): R

}
