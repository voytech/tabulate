package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.ModelExportContext

/**
 * Represents a node in the model context tree.
 *
 * @property context The model export context associated with this node.
 * @property parent The parent node of this node, or null if this is the root node.
 * @property children A map of child models to their corresponding context tree nodes.
 */
internal class ModelContextTreeNode(
    @JvmSynthetic
    internal val context: ModelExportContext,
    @JvmSynthetic
    internal val parent: ModelContextTreeNode?,
    @JvmSynthetic
    internal val children: MutableMap<AbstractModel, ModelContextTreeNode> = mutableMapOf(),
) {
    /**
     * Creates a child context for the given model and adds it to the children map.
     *
     * @param child The child model for which to create a context.
     * @return The newly created child context tree node.
     * @throws IllegalArgumentException if the child model is the same as the current or parent model.
     */
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

    /**
     * Checks if any child context satisfies the given predicate.
     *
     * @param block The predicate to apply to each child context.
     * @return True if any child context satisfies the predicate, false otherwise.
     */
    @JvmSynthetic
    internal fun checkAnyChildren(block: (ModelExportContext) -> Boolean): Boolean = children.any {
        it.value.let { node -> block(node.context) || node.checkAnyChildren(block) }
    }

    /**
     * Retrieves the context of the specified child model.
     *
     * @param childModel The child model whose context is to be retrieved.
     * @return The context of the specified child model, or null if not found.
     */
    @JvmSynthetic
    fun getChildContext(childModel: AbstractModel): ModelExportContext? = children[childModel]?.context

    /**
     * Applies the given block to each child node.
     *
     * @param block The block to apply to each child node.
     */
    @JvmSynthetic
    private fun onEachChild(block: (ModelContextTreeNode) -> Unit) {
        children.values.forEach {
            block(it)
        }
    }

    /**
     * Traverses the context tree, applying the given block to each context.
     *
     * @param block The block to apply to each context.
     */
    @JvmSynthetic
    fun traverse(block: (ModelExportContext) -> Unit) {
        block(context).also { traverseChildren(block) }
    }

    /**
     * Traverses the children of the context tree, applying the given block to each child context.
     *
     * @param block The block to apply to each child context.
     */
    @JvmSynthetic
    fun traverseChildren(block: (ModelExportContext) -> Unit) {
        onEachChild { it.traverse(block) }
    }

}

/**
 * Extension function to execute a block of code on the tree node associated with this context.
 *
 * @param block The block of code to execute.
 * @return The result of the block execution.
 * @throws IllegalStateException if the context is not managed in the tree.
 */
@JvmSynthetic
internal fun <R> ModelExportContext.treeNode(block: ModelContextTreeNode.() -> R): R = instance[model].let { treeNode ->
    requireNotNull(treeNode)
    treeNode.run(block)
}

/**
 * Extension function to retrieve the tree node associated with this context.
 *
 * @return The tree node associated with this context.
 * @throws IllegalStateException if the context is not managed in the tree.
 */
@JvmSynthetic
internal fun ModelExportContext.treeNode(): ModelContextTreeNode =
    instance[model] ?: error("ModelExportContext not managed in tree.")

/**
 * Extension function to retrieve the parent context of this context.
 *
 * @return The parent context, or null if this context has no parent.
 */
@JvmSynthetic
internal fun ModelExportContext.parent(): ModelExportContext? =
    treeNode().let { treeNode -> treeNode.parent?.context }

/**
 * Extension function to retrieve the list of parent contexts of this context.
 *
 * @return A list of parent contexts, starting from the immediate parent to the root.
 */
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