package io.github.voytech.tabulate.support.mock.components

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.support.TestRenderingContext
import io.github.voytech.tabulate.support.mock.MockAttributeRenderOperation
import io.github.voytech.tabulate.support.mock.MockRenderOperation
import io.github.voytech.tabulate.support.mock.Spy

class TestTableExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Table<Any>> {

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(
            CellTextStylesAttributeTestRenderOperation(),
            Spy.operationPriorities[TextStylesAttribute::class.java] ?: 1
        )
        operation(CellBordersAttributeTestRenderOperation(), Spy.operationPriorities[BordersAttribute::class.java] ?: 1)
        operation(
            CellBackgroundAttributeTestRenderOperation(),
            Spy.operationPriorities[BackgroundAttribute::class.java] ?: 1
        )
        operation(
            CellAlignmentAttributeTestRenderOperation(),
            Spy.operationPriorities[AlignmentAttribute::class.java] ?: 1
        )
        operation(ColumnWidthAttributeTestRenderOperation(), Spy.operationPriorities[WidthAttribute::class.java] ?: 1)
        operation(RowHeightAttributeTestRenderOperation(), Spy.operationPriorities[HeightAttribute::class.java] ?: 1)
        operation(
            TemplateFileAttributeTestRenderOperation(),
            Spy.operationPriorities[TemplateFileAttribute::class.java] ?: 1
        )
    }

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(StartTableTestOperation(false))
        operation(StartColumnTestOperation(false))
        operation(StartRowTestOperation(false))
        operation(RenderRowCellTestOperation(false))
        operation(EndRowTestOperation(false))
        operation(EndColumnTestOperation(false))
        operation(EndTableTestOperation(false))
    }

    override fun provideMeasureOperations(): BuildOperations<TestRenderingContext> = {
        operation(StartTableOperation { _, _ -> })
        operation(StartColumnOperation { _, _ -> })
        operation(StartRowOperation { _, _ -> })
        operation(RenderRowCellTestOperation(true))
        operation(EndRowOperation<TestRenderingContext, Table<Any>> { _, _ -> })
        operation(EndColumnOperation { _, _ -> })
        operation(EndTableOperation { _, _ -> })
    }

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = DocumentFormat.format("spy")

}

class StartTableTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<TableStartRenderable>(TableStartRenderable::class.java, isMeasuring)

class StartColumnTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<ColumnStartRenderable>(ColumnStartRenderable::class.java, isMeasuring)

class StartRowTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<RowStartRenderable>(RowStartRenderable::class.java, isMeasuring)

class RenderRowCellTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<CellRenderable>(CellRenderable::class.java, isMeasuring)

class EndRowTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<RowEndRenderable<*>>(RowEndRenderable::class.java, isMeasuring)

class EndColumnTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<ColumnEndRenderable>(ColumnEndRenderable::class.java, isMeasuring)

class EndTableTestOperation(isMeasuring: Boolean) :
    MockRenderOperation<TableEndRenderable>(TableEndRenderable::class.java, isMeasuring)


abstract class InterceptedCellAttributeRenderOperation<T : Attribute<T>>(clazz: Class<T>) :
    MockAttributeRenderOperation<T, CellRenderable>(clazz, CellRenderable::class.java)


abstract class InterceptedRowAttributeRenderOperation<T : Attribute<T>>(clazz: Class<T>) :
    MockAttributeRenderOperation<T, RowStartRenderable>(clazz, RowStartRenderable::class.java)

abstract class InterceptedColumnAttributeRenderOperation<T : Attribute<T>>(clazz: Class<T>) :
    MockAttributeRenderOperation<T, ColumnStartRenderable>(clazz, ColumnStartRenderable::class.java)

abstract class InterceptedTableAttributeRenderOperation<T : Attribute<T>>(
    clazz: Class<T>
) : MockAttributeRenderOperation<T, TableStartRenderable>(clazz, TableStartRenderable::class.java)

class CellTextStylesAttributeTestRenderOperation :
    InterceptedCellAttributeRenderOperation<TextStylesAttribute>(TextStylesAttribute::class.java)

class CellBordersAttributeTestRenderOperation :
    InterceptedCellAttributeRenderOperation<BordersAttribute>(BordersAttribute::class.java)

class CellBackgroundAttributeTestRenderOperation :
    InterceptedCellAttributeRenderOperation<BackgroundAttribute>(BackgroundAttribute::class.java)

class CellAlignmentAttributeTestRenderOperation :
    InterceptedCellAttributeRenderOperation<AlignmentAttribute>(AlignmentAttribute::class.java)

class ColumnWidthAttributeTestRenderOperation :
    InterceptedColumnAttributeRenderOperation<WidthAttribute>(WidthAttribute::class.java)

class RowHeightAttributeTestRenderOperation :
    InterceptedRowAttributeRenderOperation<HeightAttribute>(HeightAttribute::class.java)

class TemplateFileAttributeTestRenderOperation :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(TemplateFileAttribute::class.java)