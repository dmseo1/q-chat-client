package com.dongmin.qchat.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dongmin.qchat.R
import com.dongmin.qchat.elements.ChattingRoomMemberElement
import com.dongmin.qchat.popups.MemberInfo

class ChattingRoomMemberListAdapter(context : Context) : RecyclerView.Adapter<ChattingRoomMemberListAdapter.ViewHolder>() {

    private var chattingRoomMemberList = ArrayList<ChattingRoomMemberElement>()
    private var context : Context? = context

    //하나의 뷰에는 어떤 정보들이 들어갈까요?
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // each data item is just a string in this case
        var background: LinearLayout = itemView.findViewById<View>(R.id.background) as LinearLayout
        var nickname : TextView = itemView.findViewById<View>(R.id.nickname) as TextView
        var roomMaster : ImageView = itemView.findViewById<View>(R.id.room_master) as ImageView
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) { 0 -> 1 else -> 2}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingRoomMemberListAdapter.ViewHolder {
        val v = when(viewType) {
            1 -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.element_chatting_room_member_list, parent, false)
            }
            2 -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.element_chatting_room_member_list, parent, false)
            }
            else -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.element_chatting_room_member_list, parent, false)
            }

        }
        return ChattingRoomMemberListAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ChattingRoomMemberListAdapter.ViewHolder, position: Int) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element



        try {
            holder.background.setOnClickListener {  //멤버 프로필 (MemberInfo 클래스) 띄우기
                MemberInfo(context!!).start(chattingRoomMemberList[position].userNo.toLong())
            }
            holder.nickname.text = chattingRoomMemberList[position].nickname

            //방장 표시
            when(position) {
                1 -> {
                    holder.roomMaster.visibility = View.GONE
                }
                2 -> {
                    holder.roomMaster.visibility = View.VISIBLE
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return chattingRoomMemberList.size
    }

    fun addItem(item : ChattingRoomMemberElement) {
        chattingRoomMemberList.add(item)
    }

    fun removeItem(userNo : String) {
        for(i : Int in 0..(chattingRoomMemberList.size - 1)) {
            if(chattingRoomMemberList[i].userNo == userNo) {
                chattingRoomMemberList.remove(chattingRoomMemberList[i])
                return
            }
        }
    }

    fun isRoomMaster(userNo : Long) : Boolean {
        return (chattingRoomMemberList[0].userNo.toLong() == userNo)
    }
}