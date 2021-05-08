package pl.voytech.exporter.core.attributes

import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.operations.AdaptingCellAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.CellAttributeRenderOperation
import pl.voytech.exporter.core.template.spi.AttributeRenderOperationsProvider
import pl.voytech.exporter.core.template.spi.Identifiable
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute as CellAttributeAlias

class TestAttributeRenderOperationsFactory<T> : AttributeRenderOperationsProvider<ApachePoiExcelFacade,T> {
    override fun test(t: Identifiable): Boolean = t.getFormat() == "xlsx"

    override fun createCellAttributeRenderOperations(creationContext: ApachePoiExcelFacade): Set<CellAttributeRenderOperation<T, out CellAttributeAlias>> =
        setOf(SimpleTestCellAttributeRenderOperation(creationContext))

}

data class SimpleTestCellAttribute(val valueSuffix: String) : CellAttribute<SimpleTestCellAttribute>() {

    override fun mergeWith(other: SimpleTestCellAttribute): SimpleTestCellAttribute = other
}

class SimpleTestCellAttributeRenderOperation<T>(poi: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, T, SimpleTestCellAttribute>(poi) {

    override fun attributeType(): Class<out SimpleTestCellAttribute> = SimpleTestCellAttribute::class.java

    override fun renderAttribute(context: AttributedCell, attribute: SimpleTestCellAttribute) {
        with(adaptee.assertCell(
            context.getTableId(),
            context.rowIndex,
            context.columnIndex
        )) {
            this.setCellValue("${this.stringCellValue}_${attribute.valueSuffix}")
        }
    }
}