package io.github.voytech.tabulate.core.attributes

import io.github.voytech.tabulate.core.model.attributes.CellAttribute
import io.github.voytech.tabulate.core.template.context.RowCellContext
import io.github.voytech.tabulate.core.template.operations.AdaptingCellAttributeRenderOperation
import io.github.voytech.tabulate.core.template.operations.AttributeRenderOperationsFactory
import io.github.voytech.tabulate.core.template.operations.CellAttributeRenderOperation
import io.github.voytech.tabulate.core.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.core.template.spi.Identifiable
import io.github.voytech.tabulate.impl.template.excel.wrapper.ApachePoiExcelFacade
import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute as CellAttributeAlias

class TestAttributeRenderOperationsProvider<T> : AttributeRenderOperationsProvider<ApachePoiExcelFacade,T> {

    override fun test(t: Identifiable): Boolean = t.getFormat() == "xlsx"

    override fun getAttributeOperationsFactory(creationContext: ApachePoiExcelFacade): AttributeRenderOperationsFactory<T> {
        return object : AttributeRenderOperationsFactory<T> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttributeAlias>> =
                setOf(SimpleTestCellAttributeRenderOperation(creationContext))
        }
    }
}

data class SimpleTestCellAttribute(val valueSuffix: String) : CellAttribute<SimpleTestCellAttribute>()

class SimpleTestCellAttributeRenderOperation(poi: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, SimpleTestCellAttribute>(poi) {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: SimpleTestCellAttribute) {
        with(adaptee.assertCell(
            context.getTableId(),
            context.rowIndex,
            context.columnIndex
        )) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }
}