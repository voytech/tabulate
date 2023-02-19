package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.template.RenderingContext

interface OperationsBundleProvider<CTX : RenderingContext, M : AbstractModel<M>> :
    ExportOperationsProvider<CTX, M>, MeasureOperationsProvider<CTX, M>,
    AttributeOperationsProvider<CTX, M> {
    override fun provideMeasureOperations(): BuildOperations<CTX> = {}
}