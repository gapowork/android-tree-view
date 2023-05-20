package com.gg.gapo.treeviewlib

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gg.gapo.treeviewlib.TreeViewBuilder.*
import com.gg.gapo.treeviewlib.adapter.TreeViewAdapter
import com.gg.gapo.treeviewlib.model.NodeData
import com.gg.gapo.treeviewlib.model.NodeState
import com.gg.gapo.treeviewlib.model.NodeViewData
import kotlin.math.max
import kotlin.math.min

/**
 * @author minhta
 * @since 29/09/2021
 */
class GapoTreeView<T> private constructor(
    recyclerView: RecyclerView,
    @LayoutRes itemLayoutRes: Int,
    treeItemDecoration: RecyclerView.ItemDecoration,
    nodes: List<NodeData<T>>,
    showAllNodes: Boolean,
    listener: Listener<T>,
    adapters: Pair<ConcatAdapter.Config, List<RecyclerView.Adapter<*>>>
) {

    private var filterPredicate: (NodeViewData<T>) -> Boolean = { true }
    private val nodes = mutableListOf<NodeViewData<T>>()
    private val nodesShowOnUI = mutableListOf<NodeViewData<T>>()
    private var treeViewAdapter: TreeViewAdapter<T>? = null
    private var listener: Listener<T>? = null
    private var isTreeShowing = true

    /** Builder */
    class Builder<T> private constructor(context: Context) : RecyclerViewBuilder<T>,
        ItemLayoutResBuilder<T>,
        ListenerBuilder<T> {

        //required
        private lateinit var recyclerView: RecyclerView
        private var itemLayoutRes: Int = 0
        private lateinit var listener: Listener<T>

        //optionals
        private var showAllNodes: Boolean = false
        private var itemMargin: Int =
            context.resources.getDimensionPixelSize(R.dimen.default_margin_tree_item)
        private var adapters: Pair<ConcatAdapter.Config, List<RecyclerView.Adapter<*>>>? = null
        private val nodes: MutableList<NodeData<T>> = mutableListOf()
        private var treeItemDecoration: RecyclerView.ItemDecoration? = null

        override fun withRecyclerView(recyclerView: RecyclerView): ItemLayoutResBuilder<T> =
            apply { this.recyclerView = recyclerView }

        override fun withLayoutRes(@LayoutRes itemLayoutRes: Int): ListenerBuilder<T> =
            apply { this.itemLayoutRes = itemLayoutRes }

        override fun setListener(listener: Listener<T>): Builder<T> =
            apply { this.listener = listener }

        fun addItemDecoration(itemDecoration: RecyclerView.ItemDecoration) =
            apply { this.treeItemDecoration = itemDecoration }

        fun showAllNodes(isShowAll: Boolean) = apply { this.showAllNodes = isShowAll }

        fun itemMargin(itemMargin: Int) = apply { this.itemMargin = itemMargin }

        fun addData(list: List<NodeData<T>>) = apply { this.nodes.addAll(list) }

        fun setData(list: List<NodeData<T>>) = apply {
            this.nodes.clear()
            this.nodes.addAll(list)
        }

        fun addAdapters(config: ConcatAdapter.Config, vararg adapters: RecyclerView.Adapter<*>) =
            apply {
                this.adapters = config to adapters.toList()
            }

        fun build() = GapoTreeView(
            recyclerView,
            itemLayoutRes,
            treeItemDecoration ?: DefaultTreeItemDecoration(itemMargin),
            nodes,
            showAllNodes,
            listener,
            this.adapters ?: ConcatAdapter.Config.DEFAULT to emptyList()
        )

        companion object {
            fun <T> plant(context: Context): RecyclerViewBuilder<T> = Builder(context)
        }
    }

    /** Initialize */
    init {
        this.listener = listener

        //prepare data
        val result = mutableListOf<NodeViewData<T>>()
        result.addAll(recursiveGetDepartmentChild(emptyList(), nodes).orEmpty())
        result.forEach {
            it.nodeLevel = findNodeLevel(it)
            it.isLeaf = isLeaf(result, it)
            it.isExpanded = showAllNodes
        }

        //add data to root list
        this@GapoTreeView.nodes.clear()
        this@GapoTreeView.nodes.addAll(result)

        //add data to ui list
        this@GapoTreeView.nodesShowOnUI.clear()
        this@GapoTreeView.nodesShowOnUI.addAll(
            if (showAllNodes) result.toMutableList() else result.filter { it.nodeLevel == 0 }
        )

        //prepare UI
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = if (adapters.second.isEmpty()) {
                TreeViewAdapter(itemLayoutRes, listener).also {
                    this@GapoTreeView.treeViewAdapter = it
                }
            } else {
                val listConcatAdapters = adapters.second.toMutableList().apply {
                    add(
                        TreeViewAdapter(itemLayoutRes, listener).also {
                            this@GapoTreeView.treeViewAdapter = it
                        }
                    )
                }
                ConcatAdapter(adapters.first, listConcatAdapters)
            }
            addItemDecoration(treeItemDecoration)
        }

        //update UI
        requestUpdateTree()
    }

    private fun recursiveGetDepartmentChild(
        parentIds: List<String>,
        listChild: List<NodeData<T>>?
    ): List<NodeViewData<T>>? {
        if (listChild.isNullOrEmpty()) return null
        val result = ArrayList<NodeViewData<T>>()
        listChild.forEach {
            val internalParentIds = arrayListOf<String>()
            internalParentIds.addAll(parentIds)
            internalParentIds.add(it.nodeViewId)
            result.add(
                NodeViewData(
                    data = it,
                    nodeId = it.nodeViewId,
                    parentNodeIds = parentIds.toMutableList()
                )
            )
            result.addAll(
                recursiveGetDepartmentChild(
                    internalParentIds.toMutableList(),
                    it.getNodeChild()
                ).orEmpty()
            )
        }
        return result
    }

    fun expandNode(nodeId: String) {
        val parentNodeIndex = nodesShowOnUI.indexOfFirst { it.nodeId == nodeId }
        if (parentNodeIndex == RecyclerView.NO_POSITION) return
        val parentNode = nodes.find { it.nodeId == nodeId }
        if (parentNode == null || parentNode.isLeaf) return

        parentNode.isExpanded = true
        val isLastNode = parentNodeIndex == nodesShowOnUI.size - 1

        //update parent node
        nodesShowOnUI.removeAt(parentNodeIndex)
        if (isLastNode) {
            //case last node: don't need add by index
            nodesShowOnUI.add(parentNode.copy())
        } else {
            nodesShowOnUI.add(max(0, parentNodeIndex), parentNode.copy())
        }
        //add nodes of parent node
        if (isLastNode) {
            nodesShowOnUI.addAll(
                nodes.filter { it.parentNodeIds.contains(nodeId) && it.nodeLevel == parentNode.nodeLevel + 1 }
                    .filter(filterPredicate)
            )
        } else {
            nodesShowOnUI.addAll(
                min(nodesShowOnUI.size - 1, parentNodeIndex + 1),
                nodes.filter { it.parentNodeIds.contains(nodeId) && it.nodeLevel == parentNode.nodeLevel + 1 }
                    .filter(filterPredicate)
            )
        }
        requestUpdateTree()
    }

    fun collapseNode(nodeId: String) {
        val parentNodeIndex = nodesShowOnUI.indexOfFirst { it.nodeId == nodeId }
        if (parentNodeIndex == RecyclerView.NO_POSITION) return
        val parentNode = nodes.find { it.nodeId == nodeId }
        if (parentNode == null || parentNode.isLeaf) return

        parentNode.isExpanded = false
        val isLastNode = parentNodeIndex == nodesShowOnUI.size - 1

        //update parent node
        nodesShowOnUI.removeAt(parentNodeIndex)
        if (isLastNode) {
            //case last node: don't need add by index
            nodesShowOnUI.add(parentNode.copy())
        } else {
            nodesShowOnUI.add(max(0, parentNodeIndex), parentNode.copy())
        }
        //hide nodes of parent node
        nodes.forEach {
            if (it.parentNodeIds.contains(nodeId)) {
                it.isExpanded = false
            }
        }
        nodesShowOnUI.removeAll { it.parentNodeIds.contains(nodeId) }
        requestUpdateTree()
    }

    fun setNodesState(nodeIds: List<String>, nodeState: NodeState?) {
        this.nodes.forEach {
            if (nodeIds.contains(it.nodeId)) it.nodeState = nodeState
        }
        this.nodesShowOnUI.forEach {
            if (nodeIds.contains(it.nodeId)) it.nodeState = nodeState
        }
    }

    fun clearNodesState() {
        this.nodes.forEach {
            it.nodeState = null
        }
        this.nodesShowOnUI.forEach {
            it.nodeState = null
        }
    }

    fun getNodesByState(nodeState: NodeState): List<NodeViewData<T>> {
        return nodes.filter { it.nodeState == nodeState }
    }

    fun getSelectedNodes(): List<T> {
        return nodes.filter { it.isSelected }.map { it.getData() }
    }

    fun selectNode(nodeId: String, isSelected: Boolean) {
        val node = nodes.find { it.nodeId == nodeId } ?: return
        val child = nodes.filter { it.parentNodeIds.contains(nodeId) }
        listener?.onNodeSelected(node, child, isSelected)
    }

    fun setSelectedNode(nodes: List<NodeViewData<T>>, isSelected: Boolean) {
        nodes.forEach { it.isSelected = isSelected }
        nodes.forEach { updatedNote ->
            this.nodes.forEach {
                if (updatedNote.nodeId == it.nodeId) it.isSelected = isSelected
            }
            this.nodesShowOnUI.forEach {
                if (updatedNote.nodeId == it.nodeId) it.isSelected = isSelected
            }
        }
    }

    fun clearNodesSelected() {
        this.nodes.forEach {
            it.isSelected = false
        }
        this.nodesShowOnUI.forEach {
            it.isSelected = false
        }
    }

    fun hideTree() {
        isTreeShowing = false
        treeViewAdapter?.submitList(emptyList())
    }

    fun showTree() {
        isTreeShowing = true
        requestUpdateTree()
    }

    fun requestUpdateTree() {
        if (isTreeShowing) {
            treeViewAdapter?.submitList(nodesShowOnUI.map { it.copy() })
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun filter(predicate: (NodeViewData<T>) -> Boolean) {
        this.filterPredicate = predicate

        val filterNodes = nodes.filter(predicate)

        nodesShowOnUI.clear()
        nodesShowOnUI.addAll(filterNodes)

        // expand all parent nodes
        filterNodes.forEach { nodeViewData ->
            val parentNodeIds = nodeViewData.parentNodeIds
            parentNodeIds.forEach { parentNodeId ->
                nodesShowOnUI.find { it.nodeId == parentNodeId }?.isExpanded = true
            }

            val data = nodeViewData.getData() as NodeData<T>
            val hierarchyList = data.getHierarchyNodeChild() as List<NodeData<T>>
            if (hierarchyList.size <= 1) {
                nodeViewData.isExpanded = false
                return@forEach
            }

            val isContainChildren =
                hierarchyList.subList(1, hierarchyList.size).any { hierarchyNodeData ->
                    nodesShowOnUI.any { it.nodeId == hierarchyNodeData.nodeViewId }
                }

            nodeViewData.isExpanded = isContainChildren
            nodeViewData.isLeaf = !isContainChildren
        }

        requestUpdateTree()
    }

    private fun findNodeLevel(node: NodeViewData<T>): Int {
        return node.parentNodeIds.size
    }

    private fun isLeaf(nodes: List<NodeViewData<T>>, node: NodeViewData<T>): Boolean {
        nodes.forEach {
            if (it.parentNodeIds.contains(node.nodeId)) {
                return false
            }
        }
        return true
    }

    interface Listener<T> {
        fun onBind(holder: View, position: Int, item: NodeViewData<T>, bundle: Bundle?)

        fun onNodeSelected(
            node: NodeViewData<T>,
            child: List<NodeViewData<T>>,
            isSelected: Boolean
        )
    }

}