package io.github.voytech.tabulate.core.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.RenderingContext

interface OperationsBundleProvider<CTX : RenderingContext, M : AbstractModel> :
    ExportOperationsProvider<CTX, M>, MeasureOperationsProvider<CTX, M>,
    AttributeOperationsProvider<CTX, M> {
    override fun provideMeasureOperations(): BuildOperations<CTX> = {}
}