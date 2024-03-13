package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.operation.Renderable

abstract class DirectlyRenderableModel<R : Renderable<SimpleLayout>> : ModelWithAttributes() {

    override fun doExport(api: ExportApi) = api {
        render(asRenderable()).let { result ->
            iterations { catchOverflow(result) }
        }
    }

    override fun takeMeasures(api: ExportApi) = api {
        measure(asRenderable()).let { result ->
            iterations { catchOverflow(result) }
        }
    }

    protected abstract fun ExportApi.asRenderable(): R

}
