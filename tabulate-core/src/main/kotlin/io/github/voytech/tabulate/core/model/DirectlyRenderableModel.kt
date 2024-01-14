package io.github.voytech.tabulate.core.model

import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.operation.Renderable

abstract class DirectlyRenderableModel<R : Renderable<SimpleLayout>> : ModelWithAttributes() {

    override fun doExport(api: ExportApi) = api {
        render(asRenderable())
    }

    override fun takeMeasures(api: ExportApi) = api {
        measure(asRenderable())
    }

    protected abstract fun ExportApi.asRenderable(): R

}
