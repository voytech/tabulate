package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.layout.AttachedLayout


sealed class TreeNode<M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>>(
    val template: E,
    val context: C,
) {
    private val children: MutableList<TreeNode<*, *, *>> = mutableListOf()
    var layout: AttachedLayout<M, E, C>? = null

    internal fun dropLayout() {
        layout = null
    }

    @Suppress("UNCHECKED_CAST")
    fun <ML : AbstractModel<EL, ML, CL>, EL : ExportTemplate<EL, ML, CL>, CL : TemplateContext<CL, ML>> appendChild(ctx: CL): TreeNode<ML, EL, CL> =
        getRoot().let { rootNode ->
            BranchNode(ctx.model.template as EL, ctx, this, rootNode).let { newNode ->
                rootNode.nodes[ctx.model] = newNode
                children.add(newNode)
                newNode
            }
        }

    abstract fun getRoot(): RootNode<*, *, *>

    abstract fun getParent(): TreeNode<*, *, *>?

    internal fun traverse(action: (TreeNode<*, *, *>) -> Unit) {
        action(this).also {
            children.forEach { child -> child.traverse(action) }
        }
    }

    internal fun forChildren(action: (TreeNode<*, *, *>) -> Unit) {
        children.forEach { action(it) }
    }

}

class BranchNode<M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>>(
    template: E,
    context: C,
    internal val parent: TreeNode<*, *, *>,
    internal val root: RootNode<*, *, *>,
) : TreeNode<M, E, C>(template, context) {

    override fun getRoot(): RootNode<*, *, *> = root
    override fun getParent(): TreeNode<*, *, *> = parent
}

class RootNode<M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>>(
    template: E,
    context: C,
) : TreeNode<M, E, C>(template, context) {
    internal lateinit var activeNode: TreeNode<*, *, *>
    internal val nodes: MutableMap<Model<*, *>, TreeNode<*, *, *>> = mutableMapOf()
    internal var suspendedNodes: MutableSet<TemplateContext<*, *>> = mutableSetOf()

    init {
        nodes[context.model] = this
    }

    override fun getRoot(): RootNode<*, *, *> = this

    override fun getParent(): TreeNode<*, *, *>? = null
}

fun TreeNode<*, *, *>.setActive() = with(getRoot()) {
    activeNode = this@setActive
}

fun TreeNode<*, *, *>.endActive() = with(getRoot()) {
    activeNode = when (activeNode) {
        is BranchNode<*, *, *> -> (activeNode as BranchNode<*, *, *>).parent
        else -> this
    }
}

fun TreeNode<*, *, *>.preserveActive(block: () -> Unit) = with(getRoot()) {
    val preserved = activeNode
    block()
    activeNode = preserved
}
