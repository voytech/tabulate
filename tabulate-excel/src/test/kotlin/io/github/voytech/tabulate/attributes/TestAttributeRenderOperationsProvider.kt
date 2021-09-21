package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.excel.template.poi.ApachePoiRenderingContext
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory
import io.github.voytech.tabulate.template.operations.BaseCellAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.CellAttributeRenderOperation
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute as CellAttributeAlias

class FakeContext: RenderingContext

class FakeTestAttributeRenderOperationsProvider<T> : AttributeRenderOperationsProvider<T, FakeContext> {

    override fun getAttributeOperationsFactory(renderingContext: FakeContext): AttributeRenderOperationsFactory<T> {
        return object : AttributeRenderOperationsFactory<T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttributeAlias>> =
                setOf(FakeSimpleTestCellAttributeRenderOperation(renderingContext))
        }
    }

    override fun getContextClass(): Class<FakeContext> = FakeContext::class.java
}

class TestAttributeRenderOperationsProvider<T> : AttributeRenderOperationsProvider<T, ApachePoiRenderingContext> {

    override fun getAttributeOperationsFactory(renderingContext: ApachePoiRenderingContext): AttributeRenderOperationsFactory<T> {
        return object : AttributeRenderOperationsFactory<T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttributeAlias>> =
                setOf(SimpleTestCellAttributeRenderOperation(renderingContext))
        }
    }

    override fun getContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
}

data class SimpleTestCellAttribute(val valueSuffix: String) : CellAttribute<SimpleTestCellAttribute>() {

    class Builder(var valueSuffix: String = "") : CellAttributeBuilder<SimpleTestCellAttribute>() {
        override fun provide(): SimpleTestCellAttribute = SimpleTestCellAttribute(valueSuffix)
    }
}

class SimpleTestCellAttributeRenderOperation(poi: ApachePoiRenderingContext) :
    BaseCellAttributeRenderOperation<ApachePoiRenderingContext, SimpleTestCellAttribute>(poi) {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: SimpleTestCellAttribute) {
        with(renderingContext.provideCell(
            context.getTableId(),
            context.rowIndex,
            context.columnIndex
        )) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }
}

class FakeSimpleTestCellAttributeRenderOperation(fakeContext: FakeContext) :
    BaseCellAttributeRenderOperation<FakeContext, SimpleTestCellAttribute>(fakeContext) {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: SimpleTestCellAttribute) {

    }
}

fun <T> CellLevelAttributesBuilderApi<T>.simpleTestCellAttrib(block: SimpleTestCellAttribute.Builder.() -> Unit) =
    attribute(SimpleTestCellAttribute.Builder().apply(block))
