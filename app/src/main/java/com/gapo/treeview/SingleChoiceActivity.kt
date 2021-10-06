package com.gapo.treeview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.gg.gapo.treeviewlib.GapoTreeView
import com.gg.gapo.treeviewlib.model.NodeViewData

class SingleChoiceActivity : AppCompatActivity(), GapoTreeView.Listener<SampleModel> {

    private lateinit var treeView: GapoTreeView<SampleModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_choice)

        treeView = GapoTreeView.Builder.plant<SampleModel>(this)
            .withRecyclerView(findViewById(R.id.tree_view))
            .withLayoutRes(R.layout.single_node_view_item)
            .setListener(this)
            .setData(SampleModel.getList().toMutableList())
            .build()
    }

    override fun onBind(
        holder: View,
        position: Int,
        item: NodeViewData<SampleModel>,
        bundle: Bundle?
    ) {
        val ivArrow = holder.findViewById<AppCompatImageView>(R.id.iv_arrow)
        val rbCheck = holder.findViewById<AppCompatRadioButton>(R.id.rb_check)
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

        rbCheck.isChecked = item.isSelected

        //select node
        rbCheck.setOnClickListener {
            treeView.selectNode(item.nodeId, !item.isSelected) // will trigger onNodeSelected
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
        if (!isSelected) return // prevent unselect node

        treeView.clearNodesSelected()
        treeView.setSelectedNode(listOf(node), isSelected)
        treeView.requestUpdateTree()
    }
}