package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.components.table.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.operation.CellAttributeRenderOperation
import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.factories.AttributeOperationsFactory
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.testsupport.TestRenderingContext


class NoopTestAttributeRenderOperationsProvider : AttributeOperationsFactory<TestRenderingContext, Table<*>> {

     override fun createAttributeOperations(): Set<AttributeOperation<TestRenderingContext, Table<*>, *, *, *>> = setOf(
         NoopSimpleTestCellAttributeRenderOperation()
     )

    override fun getRenderingContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java

    override fun getRootModelClass(): Class<Table<*>> = Table::class.java

}

class TestAttributeRenderOperationsProvider : AttributeOperationsFactory<ApachePoiRenderingContext, Table<*>> {

    override fun createAttributeOperations(): Set<AttributeOperation<ApachePoiRenderingContext, Table<*>, *, *, *>> = setOf(
        SimpleTestCellAttributeRenderOperation()
    )
    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun getRootModelClass(): Class<Table<*>> = Table::class.java
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
