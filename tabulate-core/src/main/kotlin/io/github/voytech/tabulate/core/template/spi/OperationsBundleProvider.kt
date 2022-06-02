package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext

interface OperationsBundleProvider<CTX : RenderingContext, MDL : Model<MDL>> : ExportOperationsProvider<CTX, MDL>,
    AttributeOperationsProvider<CTX, MDL>