package com.gg.gapo.treeviewlib.model

import android.os.Bundle

/**
 * @author minhta
 * @since 29/09/2021
 */
interface NodeData<T> {
    fun getNodeChild(): List<NodeData<T>>

    val nodeViewId: String

    fun areItemsTheSame(item: NodeData<T>): Boolean

    fun areContentsTheSame(item: NodeData<T>): Boolean

    fun getChangePayload(item: NodeData<T>): Bundle
}