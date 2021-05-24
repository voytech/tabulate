package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.excel.template.wrapper.ApachePoiExcelFacade
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.operations.BaseCellAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory
import io.github.voytech.tabulate.template.operations.CellAttributeRenderOperation
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.template.spi.Identifiable
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute as CellAttributeAlias

class TestAttributeRenderOperationsProvider<T> : AttributeRenderOperationsProvider<ApachePoiExcelFacade,T> {

    override fun test(t: Identifiable): Boolean = t.getFormat() == "xlsx"

    override fun getAttributeOperationsFactory(creationContext: ApachePoiExcelFacade): AttributeRenderOperationsFactory<T> {
        return object : AttributeRenderOperationsFactory<T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttributeAlias>> =
                setOf(SimpleTestCellAttributeRenderOperation(creationContext))
        }
    }
}

data class SimpleTestCellAttribute(val valueSuffix: String) : CellAttribute<SimpleTestCellAttribute>() {

    class Builder(var valueSuffix: String = "") : CellAttributeBuilder<SimpleTestCellAttribute> {
        override fun build(): SimpleTestCellAttribute = SimpleTestCellAttribute(valueSuffix)
    }
}

class SimpleTestCellAttributeRenderOperation(poi: ApachePoiExcelFacade) :
    BaseCellAttributeRenderOperation<ApachePoiExcelFacade, SimpleTestCellAttribute>(poi) {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: SimpleTestCellAttribute) {
        with(renderingContext.assertCell(
            context.getTableId(),
            context.rowIndex,
            context.columnIndex
        )) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }

}

fun <T> CellLevelAttributesBuilderApi<T>.simpleTestCellAttrib(block: SimpleTestCellAttribute.Builder.() -> Unit) =
    attribute(SimpleTestCellAttribute.Builder().apply(block).build())
