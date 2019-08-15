package com.dongmin.qchat.adapters

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.ChattingRoom
import com.dongmin.qchat.elements.ChattingRoomElement
import com.dongmin.qchat.popups.RoomInputPassword

class ChattingRoomListAdapter(context : Context) : RecyclerView.Adapter<ChattingRoomListAdapter.ViewHolder>() {

    var chattingRoomList = ArrayList<ChattingRoomElement>()
    private var context : Context? = context

    //하나의 뷰에는 어떤 정보들이 들어갈까요?
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // each data item is just a string in this case
        var background: LinearLayout = itemView.findViewById<View>(R.id.background) as LinearLayout
        var title: TextView = itemView.findViewById<View>(R.id.title) as TextView
        var numOfPeople: TextView = itemView.findViewById<View>(R.id.num_of_people) as TextView
        var pType: ImageView = itemView.findViewById<View>(R.id.p_type) as ImageView
        var qType: TextView = itemView.findViewById<View>(R.id.q_type) as TextView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingRoomListAdapter.ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_chatting_room_list, parent, false)
        return ChattingRoomListAdapter.ViewHolder(v)
    }


    override fun onBindViewHolder(holder: ChattingRoomListAdapter.ViewHolder, position: Int) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element

        try {
            holder.background.setOnClickListener {
                if (Integer.parseInt(chattingRoomList[position].currentPeople) < 1) { //방이 완전이 생성되기 전
                    Toast.makeText(context, "지금은 입장할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    if (chattingRoomList[position].pType == "private") { //비밀방
                        val intent = Intent(context, RoomInputPassword::class.java)
                        intent.putExtra("room_id", chattingRoomList[position].roomId)
                        intent.putExtra("c_time", chattingRoomList[position].cTime)
                        intent.putExtra("password", chattingRoomList[position].password)
                        intent.putExtra("title", chattingRoomList[position].title)
                        intent.putExtra("p_type", chattingRoomList[position].pType)
                        intent.putExtra("q_type", chattingRoomList[position].qType)
                        intent.putExtra("max_people", chattingRoomList[position].maxPeople)
                        context!!.startActivity(intent)
                    } else {  //공개방
                        val intent = Intent(context, ChattingRoom::class.java)
                        intent.putExtra("room_id", chattingRoomList[position].roomId)
                        intent.putExtra("c_time", chattingRoomList[position].cTime)
                        intent.putExtra("password", chattingRoomList[position].password)
                        intent.putExtra("title", chattingRoomList[position].title)
                        intent.putExtra("p_type", chattingRoomList[position].pType)
                        intent.putExtra("q_type", chattingRoomList[position].qType)
                        intent.putExtra("max_people", chattingRoomList[position].maxPeople)
                        context!!.startActivity(intent)
                    }
                }
            }


            holder.title.text = chattingRoomList[position].title
            holder.numOfPeople.text = context!!.resources.getString(R.string.game_room_element_people_info,
                chattingRoomList[position].currentPeople,  chattingRoomList[position].maxPeople)   // curPeople / MaxPeople 로 표시

            if (chattingRoomList[position].pType == "public") {
                holder.pType.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ico_chat_public))
            } else {
                holder.pType.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ico_chat_private))
            }

            holder.qType.text = chattingRoomList[position].qType

        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return chattingRoomList.size
    }

    fun addItem(item : ChattingRoomElement) {
        chattingRoomList.add(item)
    }
}
