package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.ModelExportContext

internal class ModelContextTreeNode(
    @JvmSynthetic
    internal val context: ModelExportContext,
    @JvmSynthetic
    internal val parent: ModelContextTreeNode?,
    @JvmSynthetic
    internal val children: MutableMap<AbstractModel, ModelContextTreeNode> = mutableMapOf(),
) {

    @JvmSynthetic
    internal fun createChildContext(child: AbstractModel): ModelContextTreeNode {
        require(child != context.model && child != parent?.context?.model)
        return ModelExportContext(
            instance = context.instance,
            model = child,
            customStateAttributes = context.customStateAttributes,
            parentAttributes = context.parentAttributes
        ).let { childContext ->
            ModelContextTreeNode(childContext, this).also {
                children[child] = it
                context.instance[child] = it
            }
        }
    }

    @JvmSynthetic
    internal fun checkAnyChildren(block: (ModelExportContext) -> Boolean): Boolean = children.any {
        it.value.let { node -> block(node.context) || node.checkAnyChildren(block) }
    }

    @JvmSynthetic
    fun getChildContext(childModel: AbstractModel): ModelExportContext? = children[childModel]?.context

    @JvmSynthetic
    private fun onEachChild(block: (ModelContextTreeNode) -> Unit) {
        children.values.forEach {
            block(it)
        }
    }

    @JvmSynthetic
    fun traverse(block: (ModelExportContext) -> Unit) {
        block(context).also { traverseChildren(block) }
    }

    @JvmSynthetic
    fun traverseChildren(block: (ModelExportContext) -> Unit) {
        onEachChild { it.traverse(block) }
    }

}

@JvmSynthetic
internal fun <R> ModelExportContext.treeNode(block: ModelContextTreeNode.() -> R): R = instance[model].let { treeNode ->
    requireNotNull(treeNode)
    treeNode.run(block)
}

@JvmSynthetic
internal fun ModelExportContext.treeNode(): ModelContextTreeNode =
    instance[model] ?: error("ModelExportContext not managed in tree.")

@JvmSynthetic
internal fun ModelExportContext.parent(): ModelExportContext? =
    treeNode().let { treeNode -> treeNode.parent?.context }

@JvmSynthetic
internal fun ModelExportContext.parents(): List<ModelExportContext> {
    var current = this.parent()
    val parents = mutableListOf<ModelExportContext>()
    while (current!=null) {
        parents+=current
        current = current.parent()
    }
    return parents
}