package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.operation.Renderable

abstract class DirectlyRenderableModel<R : Renderable<SimpleLayout>> : ModelWithAttributes() {

    override fun doExport(api: ExportApi) = api {
        val renderable = asRenderable()
        render(renderable).let { result ->
            traceSection("Rendered: $renderable, ${result.status}")
            iterations { catchOverflow(result) }
        }
    }

    override fun takeMeasures(api: ExportApi) = api {
        val renderable = asRenderable()
        measure(renderable).let { result ->
            traceSection("Measured: $renderable, ${result.status}")
            iterations { catchOverflow(result) }
        }
    }

    protected abstract fun ExportApi.asRenderable(): R

}
