package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.spi.RenderingContextAware
import io.github.voytech.tabulate.core.template.spi.RootModelAware

interface AttributeOperationsFactory<CTX : RenderingContext, ARM: Model> : RenderingContextAware<CTX>, RootModelAware<ARM> {
    fun createAttributeOperations(): Set<AttributeOperation<CTX, ARM,*, *, *>>
}