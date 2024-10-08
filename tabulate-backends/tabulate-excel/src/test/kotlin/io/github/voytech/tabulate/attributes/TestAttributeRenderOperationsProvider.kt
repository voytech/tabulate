package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.rendering.CellAttributeRenderOperation
import io.github.voytech.tabulate.components.table.rendering.CellRenderableEntity
import io.github.voytech.tabulate.components.table.rendering.getSheetName
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.AttributeOperationsProvider
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.testsupport.TestRenderingContext


class NoopTestAttributeRenderOperationsProvider : AttributeOperationsProvider<TestRenderingContext, Table<Any>> {

     override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
         operation(NoopSimpleTestCellAttributeRenderOperation())
     }

    override fun getRenderingContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java

    override fun getModelClass(): Class<Table<Any>> = reify()

}

class TestAttributeRenderOperationsProvider : AttributeOperationsProvider<ApachePoiRenderingContext, Table<Any>> {

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {
        operation(SimpleTestCellAttributeRenderOperation())
    }
    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun getModelClass(): Class<Table<Any>> = reify()
}

data class SimpleTestCellAttribute(val valueSuffix: String) : Attribute<SimpleTestCellAttribute>() {

    class Builder(var valueSuffix: String = "") : AttributeBuilder<SimpleTestCellAttribute>(CellRenderableEntity::class.java) {
        override fun provide(): SimpleTestCellAttribute = SimpleTestCellAttribute(valueSuffix)
    }
}

class SimpleTestCellAttributeRenderOperation : CellAttributeRenderOperation<ApachePoiRenderingContext, SimpleTestCellAttribute>() {
    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderableEntity,
        attribute: SimpleTestCellAttribute
    ) {
        with(renderingContext.provideCell(context.getSheetName(), context.getRow(), context.getColumn())) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }
}

class NoopSimpleTestCellAttributeRenderOperation : CellAttributeRenderOperation<TestRenderingContext, SimpleTestCellAttribute>() {

    override operator fun invoke(
        renderingContext: TestRenderingContext,
        context: CellRenderableEntity,
        attribute: SimpleTestCellAttribute
    ) {}
}

fun <T: Any> CellLevelAttributesBuilderApi<T>.simpleTestCellAttrib(block: SimpleTestCellAttribute.Builder.() -> Unit) =
    attribute(SimpleTestCellAttribute.Builder().apply(block))
