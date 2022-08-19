package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.layout.Layout

sealed class TreeNode<M: Model<M, C>,E : ExportTemplate<E, M,C>, C : TemplateContext<C,M>>(
    val template: E,
    val context: C,
) {
    val children: MutableList<TreeNode<*, *,*>> = mutableListOf()
    var layout: Layout<M,E,C>? = null

    internal fun dropLayout() {
        layout = null
    }

    internal fun traverse(action: (TreeNode<*,*,*>) -> Unit) {
        action(this).also {
            children.forEach { child -> child.traverse(action) }
        }
    }

    fun resume() {
        if (context.isPartiallyExported()) {
            template.onResume(context)
        }
    }
}

class BranchNode<M: Model<M, C>,E : ExportTemplate<E, M,C>, C : TemplateContext<C,M>>(
    template: E,
    context: C,
    internal val parent: TreeNode<*, *, *>,
    internal val root: RootNode<*, *, *>
) : TreeNode<M,E,C>(template, context) {

    fun getWrappingLayout(): Layout<*, *, *> = parent.layout ?: run {
        when (parent) {
            is BranchNode -> parent.getWrappingLayout()
            is RootNode -> error("No wrapping layout")
        }
    }
}

class RootNode<M: Model<M, C>,E : ExportTemplate<E, M,C>, C : TemplateContext<C,M>>(
    template: E,
    context: C,
) : TreeNode<M,E,C>(template, context)
