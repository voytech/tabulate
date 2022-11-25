package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute
import io.github.voytech.tabulate.core.template.layout.*

sealed class TreeNode<M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>>(
    val template: E,
    val context: C,
) {
    private val children: MutableList<TreeNode<*, *, *>> = mutableListOf()

    var layout: AttachedLayout<M, E, C>? = null

    var measuringLayout: DefaultLayout? = null

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

    fun getClosestLayoutAwareAncestor(): TreeNode<*, *, *> =
        getParent()?.let { parent ->
            parent.takeIf { parent.layout != null } ?: parent.getClosestLayoutAwareAncestor()
        } ?: this

    fun getClosestAncestorLayout(): AttachedLayout<*, *, *>? = getClosestLayoutAwareAncestor().layout

    fun getClosestAncestorMeasuringLayout(): DefaultLayout? = getClosestLayoutAwareAncestor().measuringLayout

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

    fun resetLayout(maxRightBottom: Position? = null) {
        with(template) { preserveActive { context.createLayoutScope(maxRightBottom = maxRightBottom) { } } }
    }
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

class AttachedLayout<M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>>(
    private val node: TreeNode<M, E, C>,
    private val layout: DefaultLayout,
    private val orientation: Orientation,
) : Layout by layout {

    private var rightBottom: Position by layout::rightBottom
    internal var measured: Boolean by layout::spacePlanned
    internal var nextLayoutLeftTop: Position? = null

    private fun extendParent(position: Position) {
        if (node !is RootNode<M, E, C>) {
            node.getClosestAncestorLayout()?.extend(position)
        }
    }

    // TODO: why need to repeat all those extends and applyOnLayout !!!
    override fun extend(position: Position) {
        extendParent(position)
        layout.extend(position)
    }

    override fun extend(width: Width) = extend(Position(rightBottom.x + width, rightBottom.y))

    override fun extend(x: X) = extend(Position(x, rightBottom.y))

    override fun extend(height: Height) = extend(Position(rightBottom.x, rightBottom.y + height))

    override fun extend(y: Y) = extend(Position(rightBottom.x, y))

    override fun LayoutElementBoundingBox.applyOnLayout() {
        extend(absoluteX + width.orZero())
        extend(absoluteY + height.orZero())
    }

    internal fun finish() {
        if (node !is RootNode<M, E, C>) {
            node.getClosestAncestorLayout()?.let { parentLayout ->
                parentLayout.nextLayoutLeftTop = if (parentLayout.orientation == Orientation.HORIZONTAL) {
                    Position(parentLayout.rightBottom.x + EPSILON, parentLayout.leftTop.y)
                } else {
                    Position(parentLayout.leftTop.x, parentLayout.rightBottom.y + EPSILON)
                }
            }
        }
    }
}

fun <R> TreeNode<*, *, *>.setMeasuringLayout(
    policy: AbstractLayoutPolicy,
    uom: UnitsOfMeasure,
    block: (DefaultLayout) -> R,
): R =
    DefaultLayout(uom, Position.start(uom), getClosestAncestorLayout()?.maxRightBottom, policy).let {
        measuringLayout = it
        block(it)
    }

fun <M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>> TreeNode<M, E, C>.setLayout(
    lazyPolicy: () -> AbstractLayoutPolicy,
    uom: UnitsOfMeasure = UnitsOfMeasure.PT,
    leftTop: Position? = null,
    maxRightBottom: Position? = null,
    orientation: Orientation? = null,
): AttachedLayout<M, E, C> = getClosestLayoutAwareAncestor().let { ancestor ->
    val wrapping = ancestor.layout
    attachLayout(
        uom = wrapping?.uom ?: uom,
        orientation = orientation ?: Orientation.HORIZONTAL,
        leftTop = leftTop ?: wrapping?.nextLayoutLeftTop ?: wrapping?.leftTop ?: Position.start(uom),
        maxRightBottom = maxRightBottom ?: getEnclosingMaxRightBottom(),
        lazyPolicy = lazyPolicy
    )
}

private fun <M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>> TreeNode<M, E, C>.attachLayout(
    lazyPolicy: () -> AbstractLayoutPolicy,
    uom: UnitsOfMeasure,
    leftTop: Position,
    maxRightBottom: Position?,
    orientation: Orientation,
): AttachedLayout<M, E, C> = DefaultLayout(
    uom = uom,
    leftTop = resolveMargins(leftTop),
    maxRightBottom = maxRightBottom,
    policy = measuringLayout?.policy ?: lazyPolicy()
).let { AttachedLayout(this, it, orientation) }.also {
    it.measured = measuringLayout?.spacePlanned ?: false
    layout = it
}

fun <M : AbstractModel<E, M, C>, E : ExportTemplate<E, M, C>, C : TemplateContext<C, M>> TreeNode<M, E, C>.createLayoutScope(
    lazyPolicy: () -> AbstractLayoutPolicy,
    uom: UnitsOfMeasure = UnitsOfMeasure.PT,
    childLeftTop: Position? = null,
    maxRightBottom: Position? = null,
    orientation: Orientation? = null,
    block: Layout.() -> Unit,
) = setLayout(lazyPolicy, uom, childLeftTop, maxRightBottom, orientation).apply(block).finish()

private fun TreeNode<*, *, *>.resolveMargins(sourcePosition: Position): Position {
    val model = (context.model as? AttributedModelOrPart<*>)
    return model?.attributes?.get(MarginsAttribute::class.java)?.let {
        Position(it.left + sourcePosition.x, it.top + sourcePosition.y)
    } ?: sourcePosition
}

fun TreeNode<*, *, *>.getEnclosingMaxRightBottom(): Position? =
    getClosestLayoutAwareAncestor().let { it.layout?.maxRightBottom }