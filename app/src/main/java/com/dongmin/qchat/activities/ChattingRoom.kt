package com.dongmin.qchat.activities

import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init.StaticData.basicGamePort
import com.dongmin.qchat.activities.Init.StaticData.basicIP
import com.dongmin.qchat.activities.Init.StaticData.userInfo
import com.dongmin.qchat.activities.Main.Companion.chattingRoomSocket
import com.dongmin.qchat.adapters.ChattingListAdapter
import com.dongmin.qchat.adapters.ChattingRankingAdapter
import com.dongmin.qchat.adapters.ChattingRoomMemberListAdapter
import com.dongmin.qchat.elements.*
import com.dongmin.qchat.library.*
import com.dongmin.qchat.maps.ChattingRoomMap
import com.dongmin.qchat.popups.DupAnswer
import com.dongmin.qchat.popups.ModifyRoomInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.text.DecimalFormat


class ChattingRoom : AppCompatActivity(), View.OnClickListener, View.OnTouchListener, ChattingRoomMap.Callback, ModifyRoomInfo.Callback, KeyboardHeightObserver {

    private val TAG = "ChattingRoom"
    //로딩 상태 표시
    private var opaWindow : ImageView? = null
    private var pgBar : ProgressBar? = null

    //핸들러
    private var handler : Handler? = null

    //전송스레드
    private var clientSender : ClientSender? = null

    //채팅방 데이터
    private val roomInfo = ChattingRoomElement()

    //퀴즈 관련
    private var serverTimeDiff = 0L
    private var quizStartTime = 0L
    private var firstQuizStartTime = 0L
    private var qBoard : TextView? = null   //전광판 텍스트뷰
    private var question : String = ""
    private var answer = ArrayList<String>()
    private var imgDupAnswer : ImageView? = null

    //랭킹 보이기
    private var rankingList : RecyclerView? = null
    private var rankingListAdapter : RecyclerView.Adapter<ChattingRankingAdapter.ViewHolder>? = null
    private var rankingListLayoutManager : RecyclerView.LayoutManager? = null

    //맵
    private var frameMap : FrameLayout? = null
    private var chattingRoomMap : ChattingRoomMap? = null
    private var onPauseCalled = false

    //뷰
    //키보드 처리

    private var root : ConstraintLayout? = null
    private var keyboardHeightProvider : KeyboardHeightProvider? = null

    //채팅
    private var chattingWrapper : ConstraintLayout? = null  //채팅 관련 뷰 전체를 감싸는 뷰
    private var txtMessage : EditText? = null
    private var btnSend : Button? = null
    private var imgChattingShowHide : ImageView? = null
    private var chattingList : RecyclerView? = null
    private var chattingListAdapter : RecyclerView.Adapter<ChattingListAdapter.ViewHolder>? = null
    private var chattingListLayoutManager : RecyclerView.LayoutManager? = null

    //드로워 메뉴
    private lateinit var btnShowDrawer : ImageView
    private lateinit var drawer : DrawerLayout
    private lateinit var lblMenuTitle : TextView
    private lateinit var lblMenuQType : TextView
    private lateinit var lblMenuPType : TextView
    private lateinit var lblMenuMyNickname : TextView
    private lateinit var lblMenuMyPoints : TextView
    private lateinit var lblMenuMyExp : TextView
    private lateinit var lblMenuNumCurrentPeople : TextView
    private lateinit var lblMenuNumMaxPeople : TextView

    private lateinit var roomMemberList : RecyclerView
    private lateinit var roomMemberListAdapter : RecyclerView.Adapter<ChattingRoomMemberListAdapter.ViewHolder>
    private lateinit var roomMemberListLayoutManager : RecyclerView.LayoutManager

    //드로워 메뉴 중 방장메뉴
    private lateinit var btnMenuEditTitle : ImageView
    private lateinit var btnMenuEditQType : ImageView
    private lateinit var btnMenuEditPType : ImageView
    private lateinit var btnMenuInvitePerson : ImageView
    private lateinit var btnMenuEditMaxPeople : ImageView

    //플래그
    private var isChattingListShowing = true
    private var isQuizContinuing : Boolean = false
    //private var isBattleTriggered : Boolean = false
    private var isAlreadyAnswered : Boolean = false
    private var firstEntered : Int = 2
    private var isOX : Boolean = false


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting_room)


        //로딩 상태로 표시
        opaWindow = findViewById(R.id.opa_window)
        pgBar = findViewById(R.id.pg_bar)
        Statics.loading(opaWindow, pgBar)

        //핸들러 연결
        handler = Handler()

        //비정상종료 대비 서비스 연결
        //taskFinished!!.attachChattingRoomSocket(chattingRoomSocket!!)

        //뷰 연결
        txtMessage = findViewById(R.id.txt_message)
        btnSend = findViewById(R.id.btn_send)
        btnSend!!.setOnClickListener(this)
        imgChattingShowHide = findViewById(R.id.img_chatting_show_hide)
        imgChattingShowHide!!.setOnClickListener(this)

        //키보드 처리
        root = findViewById(R.id.root)
        keyboardHeightProvider = KeyboardHeightProvider(this)
        root!!.post {
            keyboardHeightProvider!!.start()
        }

        //퀴즈 관련
        qBoard = findViewById(R.id.lbl_question)
        imgDupAnswer = findViewById(R.id.img_dup_answer)
        imgDupAnswer!!.setOnClickListener(this)

        //랭킹 표시
        rankingList = findViewById(R.id.ranking_list)
        rankingListAdapter = ChattingRankingAdapter(applicationContext)
        rankingListLayoutManager = LinearLayoutManager(applicationContext)
        rankingList!!.adapter = rankingListAdapter
        rankingList!!.layoutManager = rankingListLayoutManager

        //드로워 메뉴
        btnShowDrawer = findViewById(R.id.btn_show_drawer)
        btnShowDrawer.setOnClickListener(this)
        drawer = findViewById(R.id.drawer_room_info)
        lblMenuTitle = findViewById(R.id.lbl_menu_title)
        lblMenuQType = findViewById(R.id.lbl_menu_q_type)
        lblMenuPType = findViewById(R.id.lbl_menu_p_type)
        lblMenuMyNickname = findViewById(R.id.lbl_menu_my_nickname)
        lblMenuMyPoints = findViewById(R.id.lbl_menu_my_points)
        lblMenuMyExp = findViewById(R.id.lbl_menu_my_exp)
        lblMenuNumCurrentPeople = findViewById(R.id.lbl_menu_num_current_people)
        lblMenuNumMaxPeople = findViewById(R.id.lbl_menu_num_max_people)

        roomMemberList = findViewById(R.id.menu_member_list)
        roomMemberListAdapter = ChattingRoomMemberListAdapter(applicationContext)
        roomMemberListLayoutManager = LinearLayoutManager(applicationContext)
        roomMemberList.adapter = roomMemberListAdapter
        roomMemberList.layoutManager = roomMemberListLayoutManager

        //드로워 메뉴 중 방장메뉴
        btnMenuEditTitle = findViewById(R.id.btn_menu_edit_title)
        btnMenuEditPType = findViewById(R.id.btn_menu_edit_p_type)
        btnMenuEditQType = findViewById(R.id.btn_menu_edit_q_type)
        btnMenuInvitePerson = findViewById(R.id.btn_menu_invite_person)
        btnMenuEditMaxPeople = findViewById(R.id.btn_menu_edit_max_people)
        btnMenuEditTitle.setOnClickListener(this)
        btnMenuEditPType.setOnClickListener(this)
        btnMenuEditQType.setOnClickListener(this)
        btnMenuInvitePerson.setOnClickListener(this)
        btnMenuEditMaxPeople.setOnClickListener(this)


        //채팅 리스트 구성
        chattingWrapper = findViewById(R.id.chatting_wrapper)
        chattingList = findViewById(R.id.chatting_list)
        chattingListAdapter = ChattingListAdapter(applicationContext)
        chattingListLayoutManager = LinearLayoutManager(applicationContext)
        chattingList!!.adapter = chattingListAdapter
        chattingList!!.layoutManager = chattingListLayoutManager
        chattingList!!.setOnTouchListener(this)


        //응답 스레드 시작
        val receiverThread = Thread {
            chattingRoomSocket = ClientSocket(basicIP, basicGamePort)
            ClientReceiver(chattingRoomSocket!!).start()
        }
        receiverThread.start()

        //리시버 스레드가 시작될 때까지 메인 스레드는 기다린다
        try {
            receiverThread.join()
        } catch(e : InterruptedException) {
            e.printStackTrace()
        }

        //전송 스레드 구성
        clientSender = ClientSender(chattingRoomSocket!!)


        //맵 연결
        val screenSizePt = Point()
        windowManager.defaultDisplay.getSize(screenSizePt)
        chattingRoomMap = ChattingRoomMap(applicationContext, roomInfo, screenSizePt.x, screenSizePt.y)
        chattingRoomMap!!.setCallback(this)
        frameMap = findViewById(R.id.frame_map)
        frameMap!!.addView(chattingRoomMap)


        //방 생성자인지 아닌지를 구분하여 다르게 입장 처리. 방 생성자는 서버에 방 생성 정보를 보내야 한다.
        //서버에 방 생성 정보를 보낸 후 서버로부터 응답을 받은 후에야 메인에 방이 있다는 것을 알려 메인의 방 리스트에 추가한다.
        if(intent.getBooleanExtra("is_room_creator", false)) {
            firstEntered = 0    //방장은 최초입장 플래그 예외
            //방을 만든 후 서버로 방 정보를 보내며(전송), 서버로부터 생성 시간 등 정보를 추가로 획득(응답)하여 엘리먼트에 추가한다.
            roomInfo.title = intent.getStringExtra("title")
            roomInfo.pType = intent.getStringExtra("p_type")
            roomInfo.qType = intent.getStringExtra("q_type")
            roomInfo.currentPeople = intent.getStringExtra("current_people")
            roomInfo.maxPeople = intent.getStringExtra("max_people")
            roomInfo.password = intent.getStringExtra("password")

            //방장 메뉴를 표시한다
            handleRoomMaster(View.VISIBLE)

            //TO SEND: 1-001:;:userNo:;;userId:;:userNickname:;:title:;:maxPeople:;:qType:;:pType:;:password
            clientSender!!.send("1-001",
                userInfo.userNo.toString() + ":;:" + userInfo.userId + ":;:"+ userInfo.userNickname + ":;:" + roomInfo.title + ":;:" + roomInfo.maxPeople + ":;:" +
                        roomInfo.qType + ":;:" + roomInfo.pType + ":;:" + roomInfo.password)
        } else {

            //방 목록에 포함된 정보 원소를 이용하여 정보를 추가한다.
            //TO SEND: 1-002:;:roomId:;:userNo:;:userId:;:userNickname
            clientSender!!.send("1-002", intent.getStringExtra("room_id") + ":;:" + userInfo.userNo + ":;:" + userInfo.userId + ":;:" + userInfo.userNickname)
        }

        //초기 메시지 삽입
        HttpConnector("game/fetch_initial_message.php", "", object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {
                (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("yellow", "","","${userInfo.userNickname}님 환영합니다. 즐거운 시간 되세요~"))

                val data = JSONArray(result).getJSONArray(0)
                for(i : Int in 0..(data.length() - 1)) {
                    (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("yellow", "","",data[i].toString()))
                }
                chattingListAdapter!!.notifyDataSetChanged()
            }
        }).execute()

        //throw Exception()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            btnSend!!.id -> {

                val message = txtMessage!!.text.toString()  //edit text 에서 텍스트 추출
                txtMessage!!.setText("")                    //edit text 내용 비우기
                if(message == "") return                    //아무것도 입력하지 않고 보내는 것은 의미없음

                var correct = false
                if(isQuizContinuing) {                      //퀴즈가 진행되는 중에
                    for(i : Int in 0..(answer.size - 1)) {  //가능한 여러 답안 중에서
                        if(message.contains(answer[i])) {   //전송하려는 메시지 내에 일치하는 것이 들어있으면 정답 판정
                            correct = true                  //정답 플래그(바로 아래에서 활용)
                            break                           //정답이 하나 나오면 더 이상 비교할 필요는 없다
                        }
                    }
                }

                if(correct) {   //정답인 경우. 정답일 때에는 정답(입력 내용)이 다른 유저의 채팅창에 보이지 않도록 한다.
                    if(!isAlreadyAnswered) {    //처음 정답을 입력한 경우
                        val answeredTime =  System.nanoTime() - firstQuizStartTime
                        chattingRoomMap!!.talk(userInfo.userNo.toString(), "A_SDM__CODE_CORRECT_ANSWER")
                        clientSender!!.send("1-006", "${roomInfo.roomId}:;:${userInfo.userNo}:;:${userInfo.userNickname}:;:$answeredTime")
                        (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("orange", userInfo.userNo.toString(), userInfo.userNickname, "정답입니다!"))
                        isAlreadyAnswered = true
                    } else {                    //이미 정답을 맞힌 경우
                        chattingRoomMap!!.talk(userInfo.userNo.toString(), "A_SDM__CODE_WEIRED")
                        (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("orange", userInfo.userNo.toString(), userInfo.userNickname, "이미 정답을 맞혔습니다!"))
                    }

                    chattingListAdapter!!.notifyDataSetChanged()
                    chattingList!!.scrollToPosition(chattingListAdapter!!.itemCount - 1)

                } else {    //정답이 아닌 경우, 퀴즈가 진행되고 있지 않은 경우, 입력한 내용을 그대로 서버로 전송, 화면에 보이게 한다.

                    chattingRoomMap!!.talk(userInfo.userNo.toString(), message)
                    (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("common", userInfo.userNo.toString(), userInfo.userNickname, message))
                    chattingListAdapter!!.notifyDataSetChanged()
                    chattingList!!.scrollToPosition(chattingListAdapter!!.itemCount - 1)
                    clientSender!!.send("1-005",
                        roomInfo.roomId + ":;:" + userInfo.userNo.toString() + ":;:" + userInfo.userNickname + ":;:" + message)

                }
            }

            btnShowDrawer.id -> {
                drawer.openDrawer(GravityCompat.END)
            }

            imgChattingShowHide!!.id -> {
                when(isChattingListShowing) {
                    true -> {
                        imgChattingShowHide!!.setImageDrawable(getDrawable(R.drawable.ico_chat_show))
                        chattingList!!.visibility = View.GONE
                        isChattingListShowing = false
                    }
                    false -> {
                        imgChattingShowHide!!.setImageDrawable(getDrawable(R.drawable.ico_chat_hide))
                        chattingList!!.visibility = View.VISIBLE
                        isChattingListShowing = true
                    }
                }
            }

            imgDupAnswer!!.id -> {
                val sb = StringBuilder(20)
                for(i : Int in 0..(answer.size- 1)) {
                    sb.append(answer[i])
                    if(i != answer.size - 1) sb.append(", ")
                }
                DupAnswer(this).start(sb.toString())
            }

            //방 정보 수정메뉴(방장 전용)
            btnMenuEditTitle.id,
            btnMenuEditQType.id,
            btnMenuEditPType.id,
            btnMenuEditMaxPeople.id -> {
                val type = when(v.id) {
                    btnMenuEditTitle.id -> 1
                    btnMenuEditQType.id -> 2
                    btnMenuEditPType.id -> 3
                    btnMenuEditMaxPeople.id -> 4
                    else -> 5
                }
                val popup = ModifyRoomInfo(this, roomInfo)
                popup.setCallback(this)
                popup.start(type)
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                when(v!!.id) {
                    chattingList!!.id -> {
                        val modifiedEvent = MotionEvent.obtain(event.downTime, event.eventTime, event.action, v.x + (v.parent as View).x + event.x, v.x + (v.parent as View).y + event.y, event.metaState)
                        if (event.x < v.width * 9 / 10) {
                            chattingRoomMap!!.dispatchTouchEvent(modifiedEvent)
                        }
                    }
                }
            }
        }
        return false
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        //Toast.makeText(applicationContext, "키보드는 $height 픽셀만큼의 높이를 가진다!", Toast.LENGTH_SHORT).show()

        val param = chattingWrapper!!.layoutParams as ConstraintLayout.LayoutParams
        param.bottomMargin = height
        chattingWrapper!!.layoutParams = param
    }

    override fun onResume() {
        Log.d(TAG, "onResume Called")
        keyboardHeightProvider!!.setKeyboardHeightObserver(this)
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause Called")
        super.onPause()
        keyboardHeightProvider!!.setKeyboardHeightObserver(null)
        chattingRoomMap!!.chattingRoomPaused()
        onPauseCalled = true
    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy Called!")
        chattingRoomMap!!.destroyMap()
        chattingRoomSocket!!.close()
        keyboardHeightProvider!!.close()
        super.onDestroy()
    }

    override fun sendToServer(messageType: String, message: String) {   //채팅맵, 방정보 수정 콜백함수
        clientSender!!.send(messageType, message)
    }


    override fun emptyRoom() {  //채팅맵 콜백함수
        finish()
    }

    fun setMenuList() {
        val df = DecimalFormat("#,##0")
        lblMenuTitle.text = roomInfo.title
        lblMenuPType.text = when(roomInfo.pType) {
            "public" -> "공개"
            "private" -> "비공개"
            else -> "알 수 없음.. "
        }
        lblMenuQType.text = roomInfo.qType
        lblMenuMyNickname.text = userInfo.userNickname
        lblMenuMyPoints.text = df.format(userInfo.userPoint)
        lblMenuMyExp.text = df.format(userInfo.userExp)
        lblMenuNumCurrentPeople.text = roomInfo.currentPeople
        lblMenuNumMaxPeople.text = roomInfo.maxPeople
    }

    fun handleRoomMaster(type : Int) {
        btnMenuEditTitle.visibility = type
        btnMenuEditQType.visibility = type
        btnMenuEditPType.visibility = type
        btnMenuInvitePerson.visibility = type
        btnMenuEditMaxPeople.visibility = type
    }


    //소켓통신 ---------------------------------------------------------------------------------------------------------------------

    // 1-001 : 채팅방 생성      , 전송 메시지 형식: 1-001:;:userNo:;;userId:;:userNickname:;:title:;:maxPeople:;:qType:;:pType:;:password
    // 1-005 : 채팅 메시지 발송  , 전송 메시지 형식: 1-005:;:userNo:;:chattingContent
    // 1-006 : 정답 맞힘 알림   , 전송 메시지 형식: 1-006:;:roomId:;:userNo:;:userNickname:;:answeredTime
    private inner class ClientSender(socket : Socket) {
        var out = DataOutputStream(socket.getOutputStream())
        var messageType = ""
        var message = ""

        fun send(messageType : String, message : String) {
            this.messageType = messageType
            this.message = message
            Thread {
                try {
                    out.writeUTF("$messageType:;:$message")
                    Log.i("ClientSender $messageType", message)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private inner class ClientReceiver(socket : Socket) : Thread() {
        var din = DataInputStream(socket.getInputStream())
        override fun run() {
            Log.e("ClientReceiver 1,2", "Receiver Thread Started")
            messageLooper@ while(true) {
                try {
                    val inString = din.readUTF()
                    if(inString == "ping") continue //핑 확인 메시지는 처리하지 않는다
                    val rData = inString.split(":;:")
                    val messageType = rData[0]
                    Log.i("ClientReceiver $messageType", inString)
                    when(rData[0]) {
                        "1-001" -> {    //채팅방 생성. 응답 메시지 형식: 1-001:;:cTime
                            //채팅방 생성자가 채팅방의 추가 정보(생성시간, 채팅방 아이디)를 받아온다
                            roomInfo.cTime = rData[1]   //cTime
                            roomInfo.roomId = rData[1] + userInfo.userId
                            chattingRoomMap!!.addCharacter(userInfo)
                            handler!!.post {
                                setMenuList()   //드로워 메뉴 구성
                                (roomMemberListAdapter as ChattingRoomMemberListAdapter).addItem(ChattingRoomMemberElement(userInfo.userNo.toString(), userInfo.userNickname))  //본인 추가
                                roomMemberListAdapter.notifyDataSetChanged()
                                Statics.loaded(opaWindow, pgBar)
                            }
                        }
                        "1-002" -> {    //채팅방 접속(채팅방 생성자 외). 응답 메시지 형식: 1-002:;:roomInfo
                            //채팅방 접속자가 채팅방에 대한 정보를 받는다
                            val roomData = JSONObject(rData[1])
                            roomInfo.roomId = roomData.getString("room_id")
                            roomInfo.title = roomData.getString("title")
                            roomInfo.password = roomData.getString("password")
                            roomInfo.currentPeople = roomData.getString("current_people")
                            roomInfo.maxPeople = roomData.getString("max_people")
                            roomInfo.pType = roomData.getString("p_type")
                            roomInfo.qType = roomData.getString("q_type")
                            roomInfo.cTime = roomData.getString("c_time")

                            handler!!.post {
                                setMenuList()   //드로워 메뉴 구성
                            }

                            //채팅방에 접속해 있는 유저 정보들을 받아서 맵에 표시한다
                            for(i : Int in 2..(rData.size - 1)) {
                                val characterData = JSONObject(rData[i])

                                HttpConnector("user/fetch_user_info.php", "user_no=" + characterData.getString("user_no"), object : UIModifyAvailableListener {
                                    override fun taskCompleted(result: String?) {
                                        val data = JSONObject(result).getJSONArray("user_info").getJSONObject(0)
                                        val userInfo = UserInfo()
                                        try {
                                            userInfo.userNo = data.getString("user_no").toLong()
                                        } catch(e : JSONException) {
                                            finish()
                                        }
                                        userInfo.userId = data.getString("user_id")
                                        userInfo.userEmail = data.getString("user_email")
                                        userInfo.userNickname = data.getString("user_nickname")
                                        userInfo.userNowUsingCharacter = data.getString("user_now_using_character").toInt()
                                        userInfo.userProfileImgPathT = data.getString("user_profile_img_path_t")

                                        handler!!.post {
                                            chattingRoomMap!!.addExistCharacter(userInfo, characterData.getString("position_x").toFloat(), characterData.getString("position_y").toFloat())
                                            (roomMemberListAdapter as ChattingRoomMemberListAdapter).addItem(ChattingRoomMemberElement(userInfo.userNo.toString(), userInfo.userNickname)) //드로워 리스트에 추가
                                            roomMemberListAdapter.notifyDataSetChanged()
                                            Statics.loaded(opaWindow, pgBar)
                                        }
                                    }
                                }).execute()
                            }
                        }
                        "1-003" -> {    //채팅방 접속자 알림. 응답 메시지 형식: 1-003:;:userNo

                            //채팅방에 접속한 사람이 있으면 기존 채팅방 접속자들에게 그 유저에 대한 정보를 알린다
                            HttpConnector("user/fetch_user_info.php", "user_no=" + rData[1], object: UIModifyAvailableListener {
                                override fun taskCompleted(result: String?) {
                                    val data = JSONObject(result).getJSONArray("user_info").getJSONObject(0)
                                    val userInfo = UserInfo()

                                    //접속 유저 정보 받기
                                    userInfo.userNo = data.getString("user_no").toLong()
                                    userInfo.userId = data.getString("user_id")
                                    userInfo.userEmail = data.getString("user_email")
                                    userInfo.userNickname = data.getString("user_nickname")
                                    userInfo.userNowUsingCharacter = data.getString("user_now_using_character").toInt()
                                    userInfo.userProfileImgPathT = data.getString("user_profile_img_path_t")

                                    //맵 조정
                                    chattingRoomMap!!.addCharacter(userInfo)

                                    //들어온 본인의 입장 메시지는 출력하지 않으며, 접속자 리스트의 중복 방지를 위해 리스트 추가를 여기서는 하지 않는다.
                                    if(Init.userInfo.userNo != userInfo.userNo) {
                                        (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("yellow", userInfo.userNo.toString(), userInfo.userNickname, "${userInfo.userNickname}님이 입장하였습니다."))
                                        handler!!.post {
                                            //접속 메시지 추가
                                            chattingListAdapter!!.notifyDataSetChanged()
                                            chattingList!!.scrollToPosition(chattingListAdapter!!.itemCount - 1)

                                            //접속자 리스트에 추가
                                            (roomMemberListAdapter as ChattingRoomMemberListAdapter).addItem(
                                                ChattingRoomMemberElement(userInfo.userNo.toString(), userInfo.userNickname)    //드로워 리스트에 추가
                                            )
                                            roomMemberListAdapter.notifyDataSetChanged()

                                            //접속자수 조정(클라이언트 단)
                                            roomInfo.currentPeople = (roomInfo.currentPeople.toInt() + 1).toString()

                                            //드로워 메뉴 접속 인원 변경
                                            lblMenuNumCurrentPeople.text = roomInfo.currentPeople
                                        }
                                    }
                                }
                            }).execute()
                        }

                        "1-004" -> { //채팅방 퇴장자 알림. 응답 메시지 형식: 1-004:;:userNo:;:userNickname
                            //맵 조정
                            chattingRoomMap!!.exit(rData[1])

                            //퇴장 메시지 출력
                            (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("yellow", rData[1], "", "${rData[2]}님이 퇴장하였습니다."))
                            handler!!.post {
                                chattingListAdapter!!.notifyDataSetChanged()
                                chattingList!!.scrollToPosition(chattingListAdapter!!.itemCount - 1)

                                //드로워 리스트에서 삭제
                                (roomMemberListAdapter as ChattingRoomMemberListAdapter).removeItem(rData[1])
                                roomMemberListAdapter.notifyDataSetChanged()

                                //유저수 조정(클라이언트 단)
                                roomInfo.currentPeople = (roomInfo.currentPeople.toInt() - 1).toString()
                                //현재 접속자 수 조정(방 안에서 보이는)
                                lblMenuNumCurrentPeople.text = roomInfo.currentPeople


                                //본인이 방장이 되는 경우
                                if((roomMemberListAdapter as ChattingRoomMemberListAdapter).isRoomMaster(userInfo.userNo)) {
                                    handleRoomMaster(View.VISIBLE)
                                }
                            }
                        }

                        "1-005" -> { //채팅 내용 받기. 응답 메시지 형식: 1-005:;:userNo:;:userNickname:;:message
                            if(rData[1].toLong() != userInfo.userNo) {  //내가 아닌 상대방의 메시지에 대해서만 처리한다.
                                chattingRoomMap!!.talk(rData[1], rData[3])
                                (chattingListAdapter as ChattingListAdapter).addItem(ChattingElement("common",rData[1],rData[2],rData[3]))
                                handler!!.post {
                                    chattingListAdapter!!.notifyDataSetChanged()
                                    chattingList!!.scrollToPosition(chattingListAdapter!!.itemCount - 1)
                                }
                            }
                        }

                        "1-006" -> { //문제 받기. 응답 메시지 형식: 1-006:;:standardTime:;:question:;:isOX:;:answer:;:answer1:;:answer2:;:answer3:;:answer4

                            //OX 처리. OX 문제인 경우 채팅룸맵에도 신호를 보낸다
                            if(rData[3] == "1") {
                                isOX = true
                                chattingRoomMap!!.isOX = true
                            } else {
                                isOX = false
                            }

                            //서버타임 기록
                            roomInfo.standardTime = rData[1]
                            quizStartTime = rData[1].toLong() + 20000000000L
                            serverTimeDiff = rData[1].toLong() - System.nanoTime()


                            //새로 입장한 유저는 문제 및 서버타임 기록까지만 진행하고 20초 대기는 진행하지 않는다.
                            if(firstEntered > 1) {
                                firstEntered = 1
                                //문제와 정답
                                question = rData[2]
                                answer.clear()
                                val answerArr = rData[4].split(";")
                                for(i : Int in 0..(answerArr.size - 1)) {
                                    answer.add(answerArr[i])
                                }
                                continue@messageLooper
                            }


                            //이외는 20초 대기
                            Thread {
                                Thread.sleep(19000)
                                while(quizStartTime - (System.nanoTime() + serverTimeDiff + 20000000000L) > 0); //20초 대기(서버 동기화 완료)

                                //문제와 정답
                                question = rData[2]
                                answer.clear()
                                val answerArr = rData[4].split(";")
                                for(i : Int in 0..(answerArr.size - 1)) {
                                    answer.add(answerArr[i])
                                }

                                isQuizContinuing = true
                                handler!!.post {
                                    rankingList!!.visibility = View.GONE
                                    imgDupAnswer!!.visibility = View.GONE
                                    (rankingListAdapter as ChattingRankingAdapter).rankingList.clear()
                                    qBoard!!.text = question
                                    firstQuizStartTime = System.nanoTime()
                                }
                            }.start()
                        }

                        "1-007" -> { //문제 종료 메시지. 응답 메시지 형식: 1-007:;:ranking
                            if(!isQuizContinuing) {
                                firstEntered = 0
                                continue@messageLooper
                            } //새로 들어와서 퀴즈 진행중이라는 정보가 없다면 이 메시지는 패스한다


                            isQuizContinuing = false
                            chattingRoomMap!!.isOX = false


                            //랭킹 처리
                            if(rData.size > 1) {
                                for(i : Int in 1..(rData.size - 1)) {
                                    val element = ChattingRankingElement()
                                    element.fill(i, rData[i])
                                    (rankingListAdapter as ChattingRankingAdapter).addItem(element)

                                    //유저 UI와 클라이언트 변수에도 변경 사항을 적용
                                    if(isAlreadyAnswered && element.userNo == userInfo.userNo.toString()) {
                                        userInfo.userPoint += element.points.toInt()
                                        userInfo.userExp += element.points.toInt()
                                        handler!!.post {
                                            lblMenuMyPoints.text = userInfo.userPoint.toString()
                                            lblMenuMyExp.text = userInfo.userExp.toString()
                                        }
                                    }
                                }
                            }

                            isAlreadyAnswered = false

                            handler!!.post {
                                try {
                                    //정답 표시
                                    qBoard!!.text = applicationContext.resources.getString(R.string.activity_chatting_room_lbl_question_answer_1,
                                        answer[0])

                                    //복수정답 허용 문제일 경우 가능한 정답을 모두 볼 수 있는 아이콘 표시
                                    if(answer.size != 1) {
                                        imgDupAnswer!!.visibility = View.VISIBLE
                                    }

                                    //정답자가 한 명 이상 있을 경우 퀴즈 랭킹 UI 표시
                                    if(rData.size > 1) {
                                        rankingList!!.visibility = View.VISIBLE
                                        (rankingListAdapter as ChattingRankingAdapter).notifyDataSetChanged()
                                    }
                                } catch(e : IndexOutOfBoundsException) {
                                    Log.d(TAG, "나간 후 정답 도착..")
                                }
                            }
                        }

                        "1-008" -> {    //퀴즈가 출제되어 있지 않을 때 새로 입장한 유저에게 퀴즈 시작 신호를 따로 전송해준다.
                            isQuizContinuing = true
                            handler!!.post {
                                rankingList!!.visibility = View.GONE
                                imgDupAnswer!!.visibility = View.GONE
                                (rankingListAdapter as ChattingRankingAdapter).rankingList.clear()
                                qBoard!!.text = question
                                firstQuizStartTime = System.nanoTime()
                            }
                        }

                        "1-009" -> {    //방장에 의한 방 정보 수정 정보 받기. 응답 데이터: 1-009:;:modifyType:;:modifiedContent:;:password
                            when(rData[1]) {
                                "1" -> {
                                    roomInfo.title = rData[2]
                                    handler!!.post {
                                        lblMenuTitle.text = roomInfo.title
                                    }
                                }
                                "2" -> {
                                    roomInfo.qType = rData[2]
                                    handler!!.post {
                                        lblMenuQType.text = roomInfo.qType
                                    }

                                }
                                "3" -> {
                                    roomInfo.pType = rData[2]
                                    handler!!.post {
                                        when(roomInfo.pType) {
                                            "public" -> {
                                                lblMenuPType.text = "공개"
                                            }
                                            "private" -> {
                                                lblMenuPType.text = "비공개"
                                                roomInfo.password = rData[3]
                                            }
                                        }
                                    }
                                }
                                "4" -> {
                                    roomInfo.maxPeople = rData[2]
                                    handler!!.post {
                                        lblMenuNumMaxPeople.text = roomInfo.maxPeople
                                    }
                                }
                            }
                        }

                        "2-002" -> {
                            if(rData[1].toLong() != userInfo.userNo) {
                                chattingRoomMap!!.moveCharacter(rData[1], rData[2].toFloat(), rData[3].toFloat())
                            }
                        }
                    }
                } catch(e1 : SocketException) {
                    Log.d("ClientReceiver 1,2", "SOCKET CLOSED")
                    return
                } catch(e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

