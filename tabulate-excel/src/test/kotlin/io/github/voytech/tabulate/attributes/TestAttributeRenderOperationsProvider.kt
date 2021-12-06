package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.excel.template.ApachePoiRenderingContext
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory
import io.github.voytech.tabulate.template.operations.CellAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.RowCellContext
import io.github.voytech.tabulate.template.operations.getTableId
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.testsupport.TestRenderingContext
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute as CellAttributeAlias


class NoopTestAttributeRenderOperationsProvider<T> : AttributeRenderOperationsProvider<T, TestRenderingContext> {

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<TestRenderingContext,T> {
        return object : AttributeRenderOperationsFactory<TestRenderingContext,T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<TestRenderingContext, out CellAttributeAlias>> =
                setOf(NoopSimpleTestCellAttributeRenderOperation())
        }
    }

    override fun getContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java
}

class TestAttributeRenderOperationsProvider<T> : AttributeRenderOperationsProvider<T, ApachePoiRenderingContext> {

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<ApachePoiRenderingContext, T> {
        return object : AttributeRenderOperationsFactory<ApachePoiRenderingContext,T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<ApachePoiRenderingContext, out CellAttributeAlias>> =
                setOf(SimpleTestCellAttributeRenderOperation())
        }
    }

    override fun getContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
}

data class SimpleTestCellAttribute(val valueSuffix: String) : CellAttribute<SimpleTestCellAttribute>() {

    class Builder(var valueSuffix: String = "") : CellAttributeBuilder<SimpleTestCellAttribute>() {
        override fun provide(): SimpleTestCellAttribute = SimpleTestCellAttribute(valueSuffix)
    }
}

class SimpleTestCellAttributeRenderOperation : CellAttributeRenderOperation<ApachePoiRenderingContext, SimpleTestCellAttribute> {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: RowCellContext,
        attribute: SimpleTestCellAttribute
    ) {
        with(renderingContext.provideCell(context.getTableId(), context.getRow(), context.getColumn())) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }
}

class NoopSimpleTestCellAttributeRenderOperation : CellAttributeRenderOperation<TestRenderingContext, SimpleTestCellAttribute> {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java
    override fun renderAttribute(
        renderingContext: TestRenderingContext,
        context: RowCellContext,
        attribute: SimpleTestCellAttribute
    ) {

    }

}

fun <T> CellLevelAttributesBuilderApi<T>.simpleTestCellAttrib(block: SimpleTestCellAttribute.Builder.() -> Unit) =
    attribute(SimpleTestCellAttribute.Builder().apply(block))
