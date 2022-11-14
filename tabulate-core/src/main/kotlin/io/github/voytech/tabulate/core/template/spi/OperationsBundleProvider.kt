package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.UnconstrainedModel
import io.github.voytech.tabulate.core.template.RenderingContext

interface OperationsBundleProvider<CTX : RenderingContext, M : UnconstrainedModel<M>> :
    ExportOperationsProvider<CTX, M>, MeasureOperationsProvider<CTX, M>,
    AttributeOperationsProvider<CTX, M> {
    override fun provideMeasureOperations(): BuildOperations<CTX> = {}
}