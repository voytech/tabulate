package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.components.table.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.operation.CellAttributeRenderOperation
import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.AttributesOperations
import io.github.voytech.tabulate.core.template.spi.AttributeOperationsProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.testsupport.TestRenderingContext


class NoopTestAttributeRenderOperationsProvider : AttributeOperationsProvider<TestRenderingContext, Table<Any>> {

     override fun createAttributeOperations(): AttributesOperations<TestRenderingContext, Table<Any>> = AttributesOperations.of(
         NoopSimpleTestCellAttributeRenderOperation()
     )

    override fun getRenderingContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java

    override fun getModelClass(): Class<Table<Any>> = reify()

}

class TestAttributeRenderOperationsProvider : AttributeOperationsProvider<ApachePoiRenderingContext, Table<Any>> {

    override fun createAttributeOperations(): AttributesOperations<ApachePoiRenderingContext, Table<Any>> = AttributesOperations.of(
        SimpleTestCellAttributeRenderOperation()
    )
    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun getModelClass(): Class<Table<Any>> = reify()
}

data class SimpleTestCellAttribute(val valueSuffix: String) : CellAttribute<SimpleTestCellAttribute>() {

    class Builder(var valueSuffix: String = "") : CellAttributeBuilder<SimpleTestCellAttribute>() {
        override fun provide(): SimpleTestCellAttribute = SimpleTestCellAttribute(valueSuffix)
    }
}

class SimpleTestCellAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, SimpleTestCellAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeClass(): Class<SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java
    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: SimpleTestCellAttribute
    ) {
        with(renderingContext.provideCell(context.getSheetName(), context.getRow(), context.getColumn())) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }
}

class NoopSimpleTestCellAttributeRenderOperation :
    CellAttributeRenderOperation<TestRenderingContext, SimpleTestCellAttribute>() {
    override fun renderingContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java
    override fun attributeClass(): Class<SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java
    override fun renderAttribute(
        renderingContext: TestRenderingContext,
        context: CellContext,
        attribute: SimpleTestCellAttribute
    ) {}
}

fun <T> CellLevelAttributesBuilderApi<T>.simpleTestCellAttrib(block: SimpleTestCellAttribute.Builder.() -> Unit) =
    attribute(SimpleTestCellAttribute.Builder().apply(block))
