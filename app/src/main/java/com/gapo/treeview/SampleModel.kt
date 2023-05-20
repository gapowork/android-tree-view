package com.gapo.treeview

import android.os.Bundle
import com.gg.gapo.treeviewlib.model.NodeData

data class SampleModel(
    override val nodeViewId: String,
    val name: String,
    val child: List<SampleModel>,
) : NodeData<SampleModel> {
    override fun getNodeChild(): List<SampleModel> {
        return child
    }

    companion object {
        fun getList() = arrayListOf(
            SampleModel(
                "ID_1", "Sample 1", listOf(
                    SampleModel("ID_1_1", "Child 1.1: child of 1", emptyList()),
                    SampleModel("ID_1_2", "Child 1.2: child of 1", emptyList()),
                )
            ),
            SampleModel(
                "ID_2", "Sample 2", listOf(
                    SampleModel("ID_2_1", "Child 2.1: child of 2", emptyList()),
                    SampleModel(
                        "ID_2_2", "Child 2.2: child of 2", listOf(
                            SampleModel("ID_2_2_1", "Child of 2.2", emptyList())
                        )
                    ),
                    SampleModel(
                        "ID_2_3", "Child 2.3: child of 2", listOf(
                            SampleModel(
                                "ID_2_3_1", "Child of 2.3", listOf(
                                    SampleModel(
                                        "ID_2_3_1_1", "Child of 2.3.1", emptyList()
                                    )
                                )
                            ),
                        )
                    )
                )
            ),
            SampleModel("ID_3", "Sample 3", emptyList()),
            SampleModel("ID_4", "Sample 4", emptyList()),
            SampleModel(
                "ID_5", "Sample 5", listOf(
                    SampleModel("ID_5_1", "Child 5.1: child of 5", emptyList()),
                    SampleModel(
                        "ID_5_2", "Child 5.2: child of 5", listOf(
                            SampleModel(
                                "ID_5_2_1", "Child 5.2.1: Child of 5.2", listOf(
                                    SampleModel(
                                        "ID_5_2_1_1", "Child of 5.2.1", emptyList()
                                    ),
                                    SampleModel(
                                        "ID_5_2_1_2", "Child of 5.2.1", emptyList()
                                    )
                                )
                            ),
                        )
                    )
                )
            ),
        )
    }

    override fun areItemsTheSame(item: NodeData<SampleModel>): Boolean {
        return if (item !is SampleModel) false
        else nodeViewId == item.nodeViewId
    }

    override fun areContentsTheSame(item: NodeData<SampleModel>): Boolean {
        return if (item !is SampleModel) false
        else item.name == name && item.child.size == child.size
    }

    override fun getChangePayload(item: NodeData<SampleModel>): Bundle {
        return Bundle()
    }
}