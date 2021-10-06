package com.gg.gapo.treeviewlib.model

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil

/**
 * @author minhta
 * @since 29/09/2021
 */
data class NodeViewData<T>(
    internal val data: NodeData<T>,
    val nodeId: String,
    val parentNodeIds: List<String>,
    var isExpanded: Boolean,
    var nodeState: NodeState?,
    internal var nodeLevel: Int,
    var isLeaf: Boolean,
    var isSelected: Boolean
) {

    @Suppress("UNCHECKED_CAST")
    fun getData(): T {
        return data as T
    }

    constructor(data: NodeData<T>, nodeId: String, parentNodeIds: List<String>) : this(
        data = data,
        nodeId = nodeId,
        parentNodeIds = parentNodeIds,
        isExpanded = false,
        nodeState = null,
        nodeLevel = 0,
        isLeaf = false,
        isSelected = false
    )

    private fun internalAreItemsTheSame(item: NodeViewData<T>): Boolean {
        return nodeId == item.nodeId && data.areItemsTheSame(item.data)
    }

    private fun internalAreContentsTheSame(item: NodeViewData<T>): Boolean {
        return isExpanded == item.isExpanded
                && nodeState == item.nodeState
                && nodeLevel == item.nodeLevel
                && isSelected == item.isSelected
                && isLeaf == item.isLeaf
                && data.areContentsTheSame(item.data)
    }

    /**
     * compare [NodeViewData] changes && [T] changes
     */
    internal class DiffCallback<T> : DiffUtil.ItemCallback<NodeViewData<T>>() {
        override fun areItemsTheSame(
            oldItem: NodeViewData<T>,
            newItem: NodeViewData<T>
        ): Boolean {
            return oldItem.internalAreItemsTheSame(newItem)
        }

        override fun areContentsTheSame(
            oldItem: NodeViewData<T>,
            newItem: NodeViewData<T>
        ): Boolean {
            return oldItem.internalAreContentsTheSame(newItem)
        }

        override fun getChangePayload(oldItem: NodeViewData<T>, newItem: NodeViewData<T>): Bundle {
            return Bundle().apply {
                putAll(oldItem.data.getChangePayload(newItem))
            }
        }
    }
}