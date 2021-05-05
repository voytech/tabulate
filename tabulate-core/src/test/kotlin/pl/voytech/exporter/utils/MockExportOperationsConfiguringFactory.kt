package pl.voytech.exporter.utils

import org.reactivestreams.Publisher
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.ResultHandler
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.core.template.spi.Identifiable

class MockExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<T, Unit>() {

    override fun test(t: Identifiable): Boolean = t.getFormat() == "mock"

    override fun getExportOperationsFactory(): ExportOperationsFactory<T, Unit> =
        object : ExportOperationsFactory<T, Unit> {
            override fun createLifecycleOperations(): LifecycleOperations<T, Unit> =
                object : LifecycleOperations<T, Unit> {
                    override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, Unit>) {
                        println("Do nothing. Mock implementation")
                    }

                    override fun finish() {
                        println("Do nothing. Mock implementation")
                    }
                }

            override fun createTableRenderOperations(): TableRenderOperations<T> =
                object : TableRenderOperations<T> {
                    override fun renderRowCell(context: AttributedCell) {
                        println("cell context: $context")
                    }
                }

            override fun createTableOperation(): TableOperation<T> = object : TableOperation<T> {
                override fun createTable(builder: TableBuilder<T>): Table<T> {
                    println("create table")
                    return super.createTable(builder)
                }
            }

        }

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T>? = null

}