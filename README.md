# Gapo Tree View
Support TreeView cho RecyclerView với khả năng tùy biến cao

## Demo
<p float="left">
	<img src="/screenshots/single.gif" width="360" />
	<img src="/screenshots/multi.gif" width="360" />
</p>

## Features
- Expand/Collapse nodes
- Select/Unselect/GetSelected nodes
- Khả năng tùy biến cao với NodeState (example ở dưới)
- Hỗ trợ margin theo level node
- Hỗ trợ concat adapters
- Show/Hide Tree: Ẩn hiện item của tree, hiệu quả cao khi sử dụng với concat adapters

## Installation
Gradle
```kotlin
implementation 'vn.gapowork.android:tree-view:1.0.0-alpha01'
```

## Usage
### Chuẩn bị data đầu vào, cần extends NodeData, ví dụ:
```kotlin
data class ExampleNodeViewData(
    val itemId: String, 
    val exampleId: String, 
    val exampleName: String,
    val child: List<ExampleNodeViewData>, // MUST HAVE
) : NodeData<ExampleNodeViewData> {

    //list child
    override fun getNodeChild(): List<NodeData<ExampleNodeViewData>> {
        return child
    }

    //id của parent, phải là unique id
    override val nodeViewId: String
        get() = itemId

    //để sử dụng diff util
    override fun areItemsTheSame(item: NodeData<ExampleNodeViewData>): Boolean {
       ...
    }

    //để sử dụng diff util
    override fun areContentsTheSame(item: NodeData<ExampleNodeViewData>): Boolean {
        ...
    }
}
```

### Tạo tree 
```kotlin
val treeView = GapoTreeView()
treeView.initialize(
    recyclerView = binding.recyclerView, //recycler view sẽ được support tree view
    itemLayoutRes = R.layout.example_node_view_item, //item layout
    nodes = list.toMutableList(), //list NodeData
    showAllNodes = false, //có show hết tất cả nodes k hay chỉ show level 1
    isSupportMargin = true, //auto margin theo level của node
    listener = GapoTreeView.Listener, //listener của tree view
    adapters = arrayOf(exampleHeaderAdapter, exampleSearchAdapter) //các adapters khác để concat nếu cần
)
```

### Các hàm cơ bản của GapoTreeView:
```kotlin 
//expand node
fun expandNode(nodeId: String)

//collapse node
fun collapseNode(nodeId: String)

//select 1 node theo id. hàm này chỉ update data, k update UI và sẽ trigger [onNodeSelected] để xử lí tiếp
fun selectNode(nodeId: String, isSelected: Boolean)

//select nhiều nodes. hàm này chỉ update data, k update UI
fun setSelectedNode(nodes: List<NodeViewData<T>>, isSelected: Boolean)

//bỏ select tất cả các nodes. hàm này chỉ update data, k update UI
fun clearNodesSelected()

//lấy tất cả các nodes đang được select
fun getSelectedNodes(): List<T>

//update layout
fun requestUpdateTree()
```

### Khả năng customize cao với NodeState: 
Mục đích chính của NodeState là để customize UI. Set NodeState theo ý và sau đó update ở onBind() (xem onBind() của GapoTreeView.Listener phía dưới để rõ hơn). 
<br>Có thể tùy ý tạo các NodeState như:
```kotlin
object DisabledNodeState: NodeState
class SpecialNodeState(val label: String): NodeState

//set NodeState cho nodes. hàm này chỉ update data, k update UI
fun setNodesState(nodeIds: List<String>, nodeState: NodeState?)

//xóa state tất cả các nodes. hàm này chỉ update data, k update UI
fun clearNodesState()

//lấy các nodes theo state
fun getNodesByState(nodeState: NodeState)
```

### GapoTreeView.Listener
```kotlin
/**
 * @param [holder] view holder của recyclerview adapter, sử dụng để bind UI
 * @param [position] vị trí item hiện tại
 * @param [item] node item
 */
 override fun onBind(
    holder: View,
    position: Int,
    item: NodeViewData<ExampleNodeViewData>
){
    //sample onBind()
    val rbCheck = holder.findViewById<AppCompatRadioButton>(R.id.rb_check)
    val button = holder.findViewById<Button>(R.id.button)
    val data = item.getData() //lấy data để bind UI, ở đây là ExampleNodeViewData
    val state = item.nodeState //state của note
    if(state is DisabledNodeState) {
        //bind UI theo state
    }

    //ví dụ khi click 1 button, set state node đó thành disabled
    button.setOnClickListener {
        treeView.setNodesState(listOf(item.nodeId), DisabledNodeState)
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
        treeView.selectNode(item.nodeId, !item.isSelected) //sẽ trigger onNodeSelected
    }
}

/**
 * Được gọi sau khi call treeView.selectNode()
 * @param [node] node item
 * @param [child] child của node được select
 */
override fun onNodeSelected(
    node: NodeViewData<DepartmentNodeViewData>,
    child: List<NodeViewData<DepartmentNodeViewData>>,
    isSelected: Boolean
) {
    //ví dụ case single-choice
    if (!isSelected) return //k cho unselect

    treeView.clearNodesSelected() //xóa trạng thái đang được chọn của các nodes
    treeView.setSelectedNode(listOf(node), isSelected) //set trạng thái selected cho node hiện tại
    treeView.requestUpdateTree() //update layout
}

```

### Support Concat Adapter:
1. Tại sao cần support concat adapter? 
<br>Ví dụ 1 màn hình cần hiển thị 1 list treeview, và đồng thời có chức năng search. Với chức năng search sẽ hiển thị dạng flat/breadcrumb, như vậy có 2 cách làm: 1 là sử dụng 2 recyclerview, 2 là sử dụng 1 recyclerview với concat adapter.
<br>Nếu sử dụng GapoTreeView trong fragment, activity thì sẽ không gặp vấn đề gì, có thể dễ dàng tách thành 2 recyclerview, 1 cho TreeView, 1 cho tính năng search.
<br>Tuy nhiên nếu sử dụng 2 RecyclerView trong BottomSheetFragment sẽ có issue không thể scroll được RecyclerView cùng BottomSheet. Như vậy giải pháp ở đây là sử dụng ConcatAdapter cho 1 RecyclerView.

2. Cách sử dụng:
Trong hàm initialize() của GapoTreeView, truyền các adapters cần concat vào param adapters.
<br>Các hàm để ẩn hiện tree
```kotlin
//ẩn tree view
 fun hideTree()

//show tree view
 fun showTree()
```
Ví dụ trong case search, chúng ta cần ẩn TreeView đi khi search và show TreeView khi không muốn search nữa. Sau khi implement TreeView với ConcatAdapter, được kết quả như sau: <br>
<img src="/images/search.gif" width="360" />

### Sample
Có thể tham khảo thêm ở OrganizationDepartmentBottomSheetFragment đang implement GapoTreeView với ConcatAdapter.



