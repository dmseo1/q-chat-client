package com.dongmin.qchat.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init.StaticData.userInfo
import com.dongmin.qchat.adapters.ChattingRoomListAdapter
import com.dongmin.qchat.elements.ChattingRoomElement
import com.dongmin.qchat.popups.RoomCreate
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.net.Socket

class GameFragment : Fragment(), View.OnClickListener {

    private val onRoomStateChangedSync = ""

    private var handler : Handler? = null

    private var socket : Socket? = null
    private var roomList : RecyclerView? = null
    private var roomListSearch : RecyclerView? = null
    private var roomListAdapter : RecyclerView.Adapter<ChattingRoomListAdapter.ViewHolder>? = null
    private var roomListSearchAdapter : RecyclerView.Adapter<ChattingRoomListAdapter.ViewHolder>? = null
    private var roomListLayoutManager : RecyclerView.LayoutManager? = null
    private var roomListSearchLayoutManager : RecyclerView.LayoutManager? = null

    private var lblNowLoading : TextView? = null
    private var lblNoResult : TextView? = null
    private var lblNoRoom : TextView? = null

    private var spnCategory : Spinner? = null

    private var txtSearchContent : EditText? = null

    private var btnHelp : Button? = null
    private var btnSeeAll : Button? = null
    private var btnRefresh : ImageView? = null
    private var btnCreateRoom : ImageView? = null
    private var btnGoSearch : ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_game, container, false)

        handler = Handler()

        roomList = v.findViewById(R.id.room_list)
        roomListSearch = v.findViewById(R.id.room_list_search)
        roomListAdapter = ChattingRoomListAdapter(context!!)
        roomListSearchAdapter = ChattingRoomListAdapter(context!!)

        roomListLayoutManager = LinearLayoutManager(context)
        roomListSearchLayoutManager = LinearLayoutManager(context)
        roomList!!.layoutManager = roomListLayoutManager
        roomListSearch!!.layoutManager = roomListSearchLayoutManager

        roomList!!.adapter = roomListAdapter
        roomListSearch!!.adapter = roomListSearchAdapter

        lblNowLoading = v.findViewById(R.id.lbl_now_loading)
        lblNoResult = v.findViewById(R.id.lbl_no_result)
        lblNoRoom = v.findViewById(R.id.lbl_no_room)

        spnCategory = v.findViewById(R.id.spn_category)

        txtSearchContent = v.findViewById(R.id.txt_search_content)

        btnHelp = v.findViewById(R.id.btn_help)
        btnSeeAll = v.findViewById(R.id.btn_see_all)
        btnRefresh = v.findViewById(R.id.btn_refresh)
        btnCreateRoom = v.findViewById(R.id.btn_create_room)
        btnCreateRoom!!.setOnClickListener(this)
        btnGoSearch = v.findViewById(R.id.btn_go_search)

        //대기방에 등록
        ClientSender(socket!!, "3-001", userInfo.userNo.toString()).start()

        return v
    }


    fun fetchSocket(socket : Socket) {
        this.socket = socket
    }


    override fun onClick(v: View?) {
        when(v!!.id) {
            btnCreateRoom!!.id -> {
                RoomCreate(context!!).start()
            }
            btnRefresh!!.id -> {

            }
            btnGoSearch!!.id -> {

            }
            btnSeeAll!!.id -> {

            }
            btnHelp!!.id -> {

            }
        }
    }

    fun addRoomToRoomList(jsonString : String) {
        val roomString = JSONObject(jsonString)
        val roomInfo = ChattingRoomElement()

        Log.e("RECEIVED", jsonString)
        roomInfo.roomId = roomString.getString("room_id")
        roomInfo.cTime = roomString.getString("c_time")
        roomInfo.qType = roomString.getString("q_type")
        roomInfo.pType = roomString.getString("p_type")
        roomInfo.password = roomString.getString("password")
        roomInfo.title = roomString.getString("title")
        roomInfo.currentPeople = roomString.getString("current_people")
        roomInfo.maxPeople = roomString.getString("max_people")

        (roomListAdapter!! as ChattingRoomListAdapter).addItem(roomInfo)
    }


    fun onRoomStateChanged(state : Int, jsonRoomInfo : String) {
        // 1: 생성, 2: 인원수 변경, 3: 방 제거
        synchronized(onRoomStateChangedSync) {

            when(state) {
                1 -> {
                    Log.d("생성된 방의 정보: ", jsonRoomInfo)
                    val data = JSONObject(jsonRoomInfo)
                    val newChattingRoom = ChattingRoomElement()
                    newChattingRoom.fillFromJSON(JSONObject(jsonRoomInfo))
                    data.getString("room_id")
                    (roomListAdapter as ChattingRoomListAdapter).addItem(newChattingRoom)
                    handler!!.post {
                        roomListAdapter!!.notifyDataSetChanged()
                    }
                }
                2 -> {
                    Log.d("현재 인원수 변경", jsonRoomInfo)
                    val data = JSONObject(jsonRoomInfo)
                    val list = (roomListAdapter as ChattingRoomListAdapter).chattingRoomList
                    try {
                        for(i : Int in 0..(list.size - 1)) {
                            if(list[i].roomId == data.getString("room_id")) {
                                list[i].currentPeople = data.getString("current_people")
                            }
                        }
                        handler!!.post {
                            roomListAdapter!!.notifyDataSetChanged()
                        }
                    } catch(e : IndexOutOfBoundsException) { return }
                }
                3 -> {
                    Log.d("방 제거", jsonRoomInfo)
                    val data = JSONObject(jsonRoomInfo)
                    val list = (roomListAdapter as ChattingRoomListAdapter).chattingRoomList
                    try {
                        for(i : Int in 0..(list.size - 1)) {
                            if(list[i].roomId == data.getString("room_id")) {
                                list.remove(list[i])
                            }
                        }
                        handler!!.post {
                            roomListAdapter!!.notifyDataSetChanged()
                        }
                    } catch(e : IndexOutOfBoundsException) { return }

                }

                4 -> {
                    Log.d("방장에 의한 방 수정", jsonRoomInfo)
                    val data = JSONObject(jsonRoomInfo)
                    val list = (roomListAdapter as ChattingRoomListAdapter).chattingRoomList
                    try {
                        for(i : Int in 0..(list.size - 1)) {
                            if(list[i].roomId == data.getString("room_id")) {
                                when(data.getString("modify_type")) {
                                    "1" -> {
                                        list[i].title = data.getString("title")
                                    }
                                    "2" -> {
                                        list[i].qType = data.getString("q_type")
                                    }
                                    "3" -> {
                                        list[i].pType = data.getString("p_type")
                                        if(list[i].pType == "private") {
                                            list[i].password = data.getString("password")
                                        }
                                    }
                                    "4" -> {
                                        list[i].maxPeople = data.getString("max_people")
                                    }
                                }
                                handler!!.post {
                                    roomListAdapter!!.notifyDataSetChanged()
                                }
                                break
                            }
                        }
                    } catch(e : IndexOutOfBoundsException) {
                        return
                    }
                }

                else -> {

                }
            }
        }

    }

    // 3-002(단일 방 상태 변경 받아오기) : 3-002:;:roomId
    private inner class ClientSender(socket : Socket, val messageType : String, val message : String) : Thread() {
        var out: DataOutputStream? =  DataOutputStream(socket.getOutputStream())
        override fun run() {
            try {
                if (out != null) {
                    out!!.writeUTF("$messageType:;:$message")
                    Log.i("ClientSender $messageType", message)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}