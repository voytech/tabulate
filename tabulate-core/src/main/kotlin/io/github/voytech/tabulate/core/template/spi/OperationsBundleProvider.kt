package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.UnconstrainedModel
import io.github.voytech.tabulate.core.template.RenderingContext

interface OperationsBundleProvider<CTX : RenderingContext, M: UnconstrainedModel<M>> : ExportOperationsProvider<CTX, M>,
    AttributeOperationsProvider<CTX, M>