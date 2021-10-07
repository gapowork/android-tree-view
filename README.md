# Gapo Tree View
Support TreeView for RecyclerView with highly customizable

## Demo
<p float="left">
	<img src="/screenshots/single.gif" width="360" />
	<img src="/screenshots/multi.gif" width="360" />
</p>

## Features
- Expand/Collapse nodes
- Select/Unselect/Get Selected nodes
- Highly customizable UI with NodeState (example below)
- Support margin by node's level
- Support concat adapters
- Show/Hide Tree: Highly effective when used with concat adapters

## Installation
Gradle
```kotlin
implementation 'vn.gapowork.android:tree-view:1.0.0-alpha01'
```

## Usage
### Prepare input data, the model need extends NodeData, example:
```kotlin
data class Example(
    //required
    override val nodeViewId: String, 
    val child: List<ExampleNodeViewData>, 
    //your custom field
    val exampleId: String, 
    val exampleName: String,
) : NodeData<ExampleNodeViewData> {

    //list child
    override fun getNodeChild(): List<NodeData<Example>> {
        return child
    }

    //for diff util
    override fun areItemsTheSame(item: NodeData<Example>): Boolean {
       ...
    }

    //for diff util
    override fun areContentsTheSame(item: NodeData<Example>): Boolean {
        ...
    }
    
     //for diff util
    override fun getChangePayload(item: NodeData<Example>): Bundle {
        return Bundle()
    }
}
```

### Create Tree 
```kotlin
val treeView = GapoTreeView.Builder.plant<DepartmentNodeViewData>(Context)
    .withRecyclerView(binding.treeViewDepartment) //RecyclerView will be supported tree view
    .withLayoutRes(R.layout.department_node_view_item) //item layout
    .setListener(GapoTreeView.Listener) //listener of tree view
    .setData(List<NodeData>) //list NodeData
    .itemMargin(Int) //optional: margin by node's level. default = 24dp
    .addItemDecoration() //optional: item decoration of RecyclerView. If use this will disable feature itemMargin
    .showAllNodes(Boolean) //optional: show all nodes or just show parent node. default = false
    .addAdapters(config: ConcatAdapter.Config, adapters: List<RecyclerView.Adapter<*>>) //optional: the adapters to concat
    .build()
```

### Basic functions
```kotlin 
//expand node
fun expandNode(nodeId: String)

//collapse node
fun collapseNode(nodeId: String)

//select a node by id. This function only updates data, not UI and will trigger [onNodeSelected] for further processing
fun selectNode(nodeId: String, isSelected: Boolean)

//select multiple nodes. This function only updates data, not UI
fun setSelectedNode(nodes: List<NodeViewData<T>>, isSelected: Boolean)

//unselect all nodes. This function only updates data, not UI
fun clearNodesSelected()

//get all selected nodes
fun getSelectedNodes(): List<T>

//update layout
fun requestUpdateTree()
```

### Highly customizable with NodeState
The main purpose of NodeState is to customize the UI. Set NodeState as you need and then update at onBind() (see onBind() of GapoTreeView.Listener below for more details)
<br>You can optionally create NodeStates like:
```kotlin
object DisabledNodeState: NodeState
class SpecialNodeState(val label: String): NodeState

//set NodeState for nodes. This function only updates data, not UI
fun setNodesState(nodeIds: List<String>, nodeState: NodeState?)

//remove state for all nodes. This function only updates data, not UI
fun clearNodesState()

//get nodes by state
fun getNodesByState(nodeState: NodeState)
```

### GapoTreeView.Listener
```kotlin
/**
 * @param [holder] view holder of Recyclerview Adapter
 * @param [position] current item position
 * @param [item] node item info
 * @param [bundle] change payload
 */
 override fun onBind(
    holder: View,
    position: Int,
    item: NodeViewData<Example>,
    bundle: Bundle? 
){
    //find view by id
    val rbCheck = holder.findViewById<AppCompatRadioButton>(R.id.rb_check)
    val button = holder.findViewById<Button>(R.id.button)
    
    //get your model. This case is Example
    val data: Example = item.getData() 
    //get state of node
    val state: NodeState = item.nodeState 
    
    //suppose when click 1 button, set state that node to DisabledNodeState
    button.setOnClickListener {
        treeView.setNodesState(listOf(item.nodeId), DisabledNodeState)
    }
    
    //bind UI by NodeState
    if(state is DisabledNodeState) {
        
    }

    //expand/collapse node
    holder.setDebouncedOnClickListener {
        if (item.isExpanded) {
            treeView.collapseNode(item.nodeId)
        } else {
            treeView.expandNode(item.nodeId)
        }
    }

    //select node
    rbCheck.setOnClickListener {
        treeView.selectNode(item.nodeId, !item.isSelected) //will trigger onNodeSelected
    }
}

/**
 * Will be triggered after call treeView.selectNode()
 * @param [node] node item
 * @param [child] child of selected node
 * @param [isSelected] is selected or not
 */
override fun onNodeSelected(
    node: NodeViewData<DepartmentNodeViewData>,
    child: List<NodeViewData<DepartmentNodeViewData>>,
    isSelected: Boolean
) {
    //example case single-choice
    if (!isSelected) return //not allow unselect

    treeView.clearNodesSelected() 
    treeView.setSelectedNode(listOf(node), isSelected) 
    treeView.requestUpdateTree()
}

```

### Support Concat Adapter:
1. Why need support concat adapter? 
<br>For example, a screen needs to display a list of treeviews, and at the same time has a search function. With the search function, it will display flat/breadcrumb form, So there are 2 ways to do it: the first way is to use 2 recyclerview, the second way is to use 1 recyclerview with concat adapter.
<br>If you are using GapoTreeView in fragment, activity there won't be any problem, can be easily split into 2 recyclerviews, 1 for TreeView, 1 for search feature.
<br>However, if using 2 RecyclerView in the BottomSheetFragment, there will have a issue that can't scroll the RecyclerView and the BottomSheet together. So the solution here is to use ConcatAdapter for 1 RecyclerView.

2. Usage:
In the GapoTreeView's constructor, pass the other adapters that need concat to the adapters param.
<br>Functions to show/hide tree

```kotlin
 fun hideTree()
 
 fun showTree()
```

### Sample
- [Single choice](app/src/main/java/com/gapo/treeview/SingleChoiceActivity.kt)
- [Multi choice](app/src/main/java/com/gapo/treeview/MultiChoiceActivity.kt)


## License
~~~
Copyright (c) 2021, Gapo Technology JSC
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of Gapo Technology JSC nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL GAPO TECHNOLOGY JSC BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
~~~