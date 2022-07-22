package io.github.voytech.tabulate.excel.components.table.model.attributes

import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.TableContext
import io.github.voytech.tabulate.components.table.operation.TableStart
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.template.operation.AttributedContext

/**
 * Excel data format attribute.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellExcelDataFormatAttribute(
    val dataFormat: String
) : Attribute<CellExcelDataFormatAttribute>() {

    @TabulateMarker
    class Builder : AttributeBuilder<CellExcelDataFormatAttribute>(CellContext::class.java) {
        var value: String by observable("General", "value" to "dataFormat")
        override fun provide(): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute(value)
    }

    override fun overrideWith(other: CellExcelDataFormatAttribute): CellExcelDataFormatAttribute =
        CellExcelDataFormatAttribute(
            dataFormat = takeIfChanged(other, CellExcelDataFormatAttribute::dataFormat)
        )

    companion object {

        @JvmStatic
        inline fun builder() : Builder = Builder()
    }

}

fun <T: Any> CellLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.builder().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.builder().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.builder().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.format(block: () -> String) =
    attribute(CellExcelDataFormatAttribute.builder().apply { value = block() })

/**
 * Excel data table attribute. Enables sorting and filtering within specific column and row ranges.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class FilterAndSortTableAttribute(
    val columnRange: IntRange,
    val rowRange: IntRange
) : Attribute<FilterAndSortTableAttribute>() {

    @TabulateMarker
    class Builder : AttributeBuilder<FilterAndSortTableAttribute>(TableContext::class.java) {
        var columnRange: IntRange by observable(0..100)
        var rowRange: IntRange by observable(0..65000)
        override fun provide(): FilterAndSortTableAttribute = FilterAndSortTableAttribute(columnRange, rowRange)
    }

    override fun overrideWith(other: FilterAndSortTableAttribute): FilterAndSortTableAttribute =
        FilterAndSortTableAttribute(
            columnRange = takeIfChanged(other, FilterAndSortTableAttribute::columnRange),
            rowRange = takeIfChanged(other, FilterAndSortTableAttribute::rowRange),
        )

}

fun <T: Any> TableLevelAttributesBuilderApi<T>.filterAndSort(block: FilterAndSortTableAttribute.Builder.() -> Unit) =
    attribute(FilterAndSortTableAttribute.Builder().apply(block))


/**
 * Cell comment attribute.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
data class CellCommentAttribute(
    val author: String = "anonymous",
    val comment: String = "Please provide a commentary!"
) : Attribute<CellCommentAttribute>() {

    @TabulateMarker
    class Builder() : AttributeBuilder<CellCommentAttribute>(CellContext::class.java) {
        var author: String by observable("anonymous")
        var comment: String by observable("Please provide a commentary!")
        override fun provide(): CellCommentAttribute = CellCommentAttribute(author, comment)
    }

    override fun overrideWith(other: CellCommentAttribute): CellCommentAttribute = CellCommentAttribute(
        author = takeIfChanged(other, CellCommentAttribute::author),
        comment = takeIfChanged(other, CellCommentAttribute::comment),
    )

}

fun <T: Any> CellLevelAttributesBuilderApi<T>.comment(block: CellCommentAttribute.Builder.() -> Unit) =
    attribute(CellCommentAttribute.Builder().apply(block))


/**
 * Printing settings attribute.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
data class PrintingAttribute(
    val numberOfCopies: Short,
    val isDraft: Boolean,
    val blackAndWhite: Boolean,
    val noOrientation: Boolean,
    val leftToRight: Boolean,
    val printPageNumber: Boolean,
    val firstPageNumber: Short,
    val paperSize: Short,
    val landscape: Boolean,
    val headerMargin: Double,
    val footerMargin: Double,
    val fitHeight: Short,
    val fitWidth: Short,
    val firstPrintableColumn: Int,
    val lastPrintableColumn: Int,
    val firstPrintableRow: Int,
    val lastPrintableRow: Int,
    val footerCenter: String,
    val footerLeft: String,
    val footerRight: String,
    val headerCenter: String,
    val headerLeft: String,
    val headerRight: String,
) : Attribute<PrintingAttribute>() {

    @TabulateMarker
    class Builder : AttributeBuilder<PrintingAttribute>(TableStart::class.java) {
        var numberOfCopies: Short by observable(1)
        var isDraft: Boolean by observable(false)
        var blackAndWhite: Boolean by observable(false)
        var noOrientation: Boolean by observable(false)
        var leftToRight: Boolean by observable(false)
        var printPageNumber: Boolean by observable(false)
        var firstPageNumber: Short by observable(1)
        var paperSize: Short by observable(1)
        var landscape: Boolean by observable(false)
        var headerMargin: Double by observable(1.0)
        var footerMargin: Double by observable(1.0)
        var fitHeight: Short by observable(1)
        var fitWidth: Short by observable(1)
        var firstPrintableColumn: Int by observable(0)
        var lastPrintableColumn: Int by observable(0)
        var firstPrintableRow: Int by observable(0)
        var lastPrintableRow: Int by observable(0)
        var footerLeft: String by observable("")
        var footerCenter: String by observable("")
        var footerRight: String by observable("")
        var headerLeft: String by observable("")
        var headerCenter: String by observable("")
        var headerRight: String by observable("")

        override fun provide(): PrintingAttribute = PrintingAttribute(
            numberOfCopies, isDraft, blackAndWhite, noOrientation, leftToRight, printPageNumber,
            firstPageNumber, paperSize, landscape, headerMargin, footerMargin, fitHeight, fitWidth,
            firstPrintableColumn, lastPrintableColumn, firstPrintableRow, lastPrintableRow,
            footerCenter, footerLeft, footerRight, headerCenter, headerLeft, headerRight
        )
    }

    override fun overrideWith(other: PrintingAttribute): PrintingAttribute = PrintingAttribute(
        numberOfCopies = takeIfChanged(other, PrintingAttribute::numberOfCopies),
        isDraft = takeIfChanged(other, PrintingAttribute::isDraft),
        blackAndWhite = takeIfChanged(other, PrintingAttribute::blackAndWhite),
        noOrientation = takeIfChanged(other, PrintingAttribute::noOrientation),
        leftToRight = takeIfChanged(other, PrintingAttribute::leftToRight),
        printPageNumber = takeIfChanged(other, PrintingAttribute::printPageNumber),
        firstPageNumber = takeIfChanged(other, PrintingAttribute::firstPageNumber),
        paperSize = takeIfChanged(other, PrintingAttribute::paperSize),
        landscape = takeIfChanged(other, PrintingAttribute::landscape),
        headerMargin = takeIfChanged(other, PrintingAttribute::headerMargin),
        footerMargin = takeIfChanged(other, PrintingAttribute::footerMargin),
        fitHeight = takeIfChanged(other, PrintingAttribute::fitHeight),
        fitWidth = takeIfChanged(other, PrintingAttribute::fitWidth),
        firstPrintableColumn = takeIfChanged(other, PrintingAttribute::firstPrintableColumn),
        lastPrintableColumn = takeIfChanged(other, PrintingAttribute::lastPrintableColumn),
        firstPrintableRow = takeIfChanged(other, PrintingAttribute::firstPrintableRow),
        lastPrintableRow = takeIfChanged(other, PrintingAttribute::lastPrintableRow),
        footerLeft = takeIfChanged(other, PrintingAttribute::footerLeft),
        footerCenter = takeIfChanged(other, PrintingAttribute::footerCenter),
        footerRight = takeIfChanged(other, PrintingAttribute::footerRight),
        headerLeft = takeIfChanged(other, PrintingAttribute::headerLeft),
        headerCenter = takeIfChanged(other, PrintingAttribute::headerCenter),
        headerRight = takeIfChanged(other, PrintingAttribute::headerRight),
    )

}

fun <T: Any> TableLevelAttributesBuilderApi<T>.printing(block: PrintingAttribute.Builder.() -> Unit) =
    attribute(PrintingAttribute.Builder().apply(block))