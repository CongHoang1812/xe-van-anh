package com.example.authenticateuserandpass.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import com.example.authenticateuserandpass.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetLocationFragment : BottomSheetDialogFragment() {
    var listener: OnLocationSelectedListener? = null

    private lateinit var expandableListView: ExpandableListView
    private lateinit var searchEditText: EditText

    private lateinit var listGroup: List<String>
    private lateinit var listChild: HashMap<String, List<String>>
    private lateinit var adapter: SimpleExpandableListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        expandableListView = view.findViewById(R.id.expandableListView)
        searchEditText = view.findViewById(R.id.searchEditText)

        expandableListView.setDivider(ColorDrawable(Color.LTGRAY))
        expandableListView.dividerHeight = 1

        setupData()
        setupExpandableList()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupData() {
        listGroup = listOf("Hà Nội", "HCM", "Thanh Hóa")
        listChild = hashMapOf(
            "Hà Nội" to listOf("Sân Bay Nội Bài", "BX Nước Ngầm"),
            "HCM" to listOf("TP Thủ Đức", "Quận 1"),
            "Thanh Hóa" to listOf(
                "BX Sầm Sơn", "BX Huyền Hồng", "BX Phía Nam", "Nghi Sơn",
                "BX Phía Bắc", "Đông Sơn", "BX Thọ Xuân",
                "Thọ Xuân", "TP Thanh Hóa",
                "Quán Lào", "Thiệu Hóa", "Nga Sơn"
            )
        )
    }

    private fun setupExpandableList() {
        val groupList = listGroup.map { mapOf("GROUP_NAME" to it) }
        val childList = listChild.map {
            it.value.map { child -> mapOf("CHILD_NAME" to child) }
        }


        adapter = SimpleExpandableListAdapter(
            requireContext(),
            groupList,
            android.R.layout.simple_expandable_list_item_1,
            arrayOf("GROUP_NAME"),
            intArrayOf(android.R.id.text1),
            childList,
            R.layout.item_child,
            arrayOf("CHILD_NAME"),
            intArrayOf(R.id.tvChild)
        )


        expandableListView.setAdapter(adapter)
        // ➕ Gắn sự kiện chọn
//        expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
////            val groupName = listGroup[groupPosition]
////            val location = listChild[groupName]?.get(childPosition)
////
////            location?.let {
////                listener?.onLocationSelected(it) // Gọi callback
////                dismiss() // Đóng BottomSheet
////            }
////            true
////        }
        expandableListView.setOnChildClickListener { parent, _, groupPosition, childPosition, _ ->
            val groupMap = adapter.getGroup(groupPosition) as Map<*, *>
            val groupName = groupMap["GROUP_NAME"] as String

            val childMap = adapter.getChild(groupPosition, childPosition) as Map<*, *>
            val location = childMap["CHILD_NAME"] as String

            listener?.onLocationSelected(location)
            dismiss()
            true
        }
    }

    private fun filterData(query: String) {
        if (query.isEmpty()) {
            setupExpandableList()
            return
        }

        val filteredChild = HashMap<String, List<String>>()
        for ((group, children) in listChild) {
            val filtered = children.filter { it.contains(query, ignoreCase = true) }
            if (filtered.isNotEmpty()) {
                filteredChild[group] = filtered
            }
        }

        val filteredGroupList = filteredChild.keys.map { mapOf("GROUP_NAME" to it) }
        val filteredChildList = filteredChild.values.map {
            it.map { child -> mapOf("CHILD_NAME" to child) }
        }

        adapter = SimpleExpandableListAdapter(
            requireContext(),
            filteredGroupList,
            android.R.layout.simple_expandable_list_item_1,
            arrayOf("GROUP_NAME"),
            intArrayOf(android.R.id.text1),
            filteredChildList,
            android.R.layout.simple_list_item_1,
            arrayOf("CHILD_NAME"),
            intArrayOf(android.R.id.text1)
        )

        expandableListView.setAdapter(adapter)

        for (i in 0 until adapter.groupCount) {
            expandableListView.expandGroup(i)
        }
    }
}
interface OnLocationSelectedListener {
    fun onLocationSelected(location: String)
}