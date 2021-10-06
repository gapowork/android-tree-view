package com.gg.gapo.treeviewlib

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

interface TreeViewBuilder {
    interface RecyclerViewBuilder<T> {
        fun withRecyclerView(recyclerView: RecyclerView): ItemLayoutResBuilder<T>
    }

    interface ItemLayoutResBuilder<T> {
        fun withLayoutRes(@LayoutRes itemLayoutRes: Int): ListenerBuilder<T>
    }

    interface ListenerBuilder<T> {
        fun setListener(listener: GapoTreeView.Listener<T>): GapoTreeView.Builder<T>
    }
}