package com.gapo.treeview

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.addTextChangedListener
import com.gg.gapo.treeviewlib.GapoTreeView
import com.gg.gapo.treeviewlib.model.NodeState
import com.gg.gapo.treeviewlib.model.NodeViewData

class MultiChoiceActivity : AppCompatActivity(), GapoTreeView.Listener<SampleModel> {

    private lateinit var treeView: GapoTreeView<SampleModel>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_choice)

        treeView = GapoTreeView.Builder.plant<SampleModel>(this)
            .withRecyclerView(findViewById(R.id.tree_view))
            .withLayoutRes(R.layout.multi_node_view_item)
            .setListener(this)
            .setData(SampleModel.getList().toMutableList())
            .build()

        findViewById<AppCompatEditText>(R.id.searchEditText)?.addTextChangedListener {
            treeView.filter { nodeViewData ->
                val searchText = it?.toString()?.lowercase() ?: ""

                if (searchText.isBlank())
                    return@filter true

                // if any children match the search, show parents too
                val hierarchyList = nodeViewData.getData().getHierarchyNodeChild()
                hierarchyList.any {
                    searchText in it.name.lowercase()
                }
            }
        }
    }

    override fun onBind(
        holder: View,
        position: Int,
        item: NodeViewData<SampleModel>,
        bundle: Bundle?
    ) {
        val ivArrow = holder.findViewById<AppCompatImageView>(R.id.iv_arrow)
        val cbCheck = holder.findViewById<AppCompatCheckBox>(R.id.rb_check)
        val tvNode = holder.findViewById<AppCompatTextView>(R.id.tv_department_name)
        val data = item.getData()

        tvNode.text = data.name

        if (item.isLeaf) {
            ivArrow.visibility = View.INVISIBLE
        } else {
            ivArrow.visibility = View.VISIBLE
        }
        val rotateDegree = if (item.isExpanded) 90f else 0f
        ivArrow.rotation = rotateDegree

        cbCheck.isChecked = item.isSelected

        //select node
        cbCheck.setOnClickListener {
            treeView.selectNode(item.nodeId, !item.isSelected) // will trigger onNodeSelected
        }

        //update UI by node states
        if (item.nodeState == NodeStateDisabled) {
            cbCheck.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            cbCheck.isEnabled = false
        } else {
            cbCheck.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            cbCheck.isEnabled = true
        }

        //toggle node
        holder.setOnClickListener {
            if (item.isExpanded) {
                treeView.collapseNode(item.nodeId)
            } else {
                treeView.expandNode(item.nodeId)
            }
        }
    }

    override fun onNodeSelected(
        node: NodeViewData<SampleModel>,
        child: List<NodeViewData<SampleModel>>,
        isSelected: Boolean
    ) {
        //set selected for parent node and its child
        treeView.setSelectedNode(arrayListOf(node).apply { addAll(child) }, isSelected)

        //disable all child
        treeView.setNodesState(
            child.map { it.nodeId },
            if (isSelected) NodeStateDisabled else null
        )

        //update layout
        treeView.requestUpdateTree()
    }

    companion object {
        /** Customize node state **/
        object NodeStateDisabled : NodeState()

    }

}