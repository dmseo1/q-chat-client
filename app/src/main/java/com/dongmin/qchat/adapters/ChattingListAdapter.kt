package com.dongmin.qchat.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dongmin.qchat.R
import com.dongmin.qchat.elements.ChattingElement

class ChattingListAdapter(context : Context) : RecyclerView.Adapter<ChattingListAdapter.ViewHolder>() {

    private var chattingList = ArrayList<ChattingElement>()
    private var context : Context? = context

    //하나의 뷰에는 어떤 정보들이 들어갈까요?
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // each data item is just a string in this case
        var nickname : TextView = itemView.findViewById<View>(R.id.nickname) as TextView
        var message: TextView = itemView.findViewById<View>(R.id.message) as TextView
    }

    override fun getItemViewType(position: Int): Int {
        return when(chattingList[position].category) {
            "common" -> 1
            "yellow" -> 2
            "orange" -> 3
            "green" -> 4
            else -> 4
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingListAdapter.ViewHolder {
        // create a new view
        val v : View = when(viewType) {
            1 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_conversation, parent, false)
            2 ->  LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_notification, parent, false)
            3 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_notification, parent, false)
            4 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_notification, parent, false)
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.element_chatting_notification, parent, false)
        }
        return ChattingListAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ChattingListAdapter.ViewHolder, position: Int) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        try {
            when(chattingList[position].category) {
                "common" -> {    //일반 대화
                    holder.nickname.text = chattingList[position].userNickname
                }
                "yellow" -> {    //입장, 퇴장
                    holder.message.setTextColor(Color.parseColor("#fff000"))
                }
                "orange" -> {    //정답, 오답
                    holder.message.setTextColor(Color.parseColor("#ff901e"))
                }
                "green" -> {
                    holder.message.setTextColor(Color.parseColor("#00ff00"))
                }
            }
            holder.message.text = chattingList[position].message
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return chattingList.size
    }

    fun addItem(item : ChattingElement) {
        chattingList.add(item)
    }
}
