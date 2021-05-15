package io.github.voytech.tabulate.template.operations.impl

import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedColumn
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.narrow
import io.github.voytech.tabulate.template.operations.TableRenderOperations

@Suppress("UNCHECKED_CAST")
class AttributeAwareTableRenderOperations<T>(
    private val attributeOperations: AttributesOperations<T>,
    private val baseTableRenderOperations: TableRenderOperations<T>
) : TableRenderOperations<T> {

    override fun beginRow(context: AttributedRow<T>) {
        if (!context.rowAttributes.isNullOrEmpty()) {
            var operationRendered = false
            context.rowAttributes.forEach { attribute ->
                attributeOperations.getRowAttributeOperation(attribute.javaClass)?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableRenderOperations.beginRow(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context.narrow(), attribute)
                }
            }
        } else {
            baseTableRenderOperations.beginRow(context)
        }
    }

    override fun renderColumn(context: AttributedColumn) {
        context.columnAttributes?.let { attributes ->
            attributes.forEach { attribute ->
                attributeOperations.getColumnAttributeOperation(attribute.javaClass)
                    ?.renderAttribute(context.narrow(), attribute)
            }
        }
    }

    override fun renderRowCell(context: AttributedCell) {
        if (!context.attributes.isNullOrEmpty()) {
            var operationRendered = false
            context.attributes.forEach { attribute ->
                attributeOperations.getCellAttributeOperation(attribute.javaClass)?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableRenderOperations.renderRowCell(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context.narrow(), attribute)
                }
            }
        } else {
            baseTableRenderOperations.renderRowCell(context)
        }
    }

    override fun endRow(context: AttributedRow<T>) = baseTableRenderOperations.endRow(context)

}

