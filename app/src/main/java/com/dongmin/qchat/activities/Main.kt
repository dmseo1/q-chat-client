package com.dongmin.qchat.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import com.dongmin.qchat.fragments.FeedFragment
import com.dongmin.qchat.fragments.GameFragment
import com.dongmin.qchat.fragments.MainFragment
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init.StaticData.basicGamePort
import com.dongmin.qchat.activities.Init.StaticData.basicIP
import com.dongmin.qchat.library.ClientSocket
import com.dongmin.qchat.services.TaskFinished
import java.io.DataInputStream
import java.net.Socket
import java.net.SocketException


class Main : FragmentActivity(), BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {

    companion object {
        var taskFinished : TaskFinished? = null
        var waitingRoomSocket : ClientSocket? = null
        var chattingRoomSocket : ClientSocket? = null
    }

    private var handler : Handler? = null
    private var clientReceiver : ClientReceiver? = null
    private val tabList = arrayOf("홈", "게임하기", "소식")
    private var mainFragment :  MainFragment? = null; private var gameFragment :  GameFragment? = null; private var feedFragment :  FeedFragment? = null
    private val fragmentManager : FragmentManager = supportFragmentManager
    private var fragmentWrapper : FrameLayout? = null

    private var navigation : BottomNavigationView? = null

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //비정상종료시 소켓 연결 해제 구현
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
        //TODO (소켓 연결 해제 구현)
            try {
                waitingRoomSocket!!.close()
                chattingRoomSocket!!.close()
            } catch(e : NullPointerException) {

            }
            Log.d("비정상 종료", "비정상 종료")
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(9)
        }


        taskFinished = TaskFinished()

        //대기방 소켓 생성 및 타임아웃 설정
        val socketThread = Thread {
            waitingRoomSocket = ClientSocket(basicIP, basicGamePort)
        }
        socketThread.start()
        socketThread.join()

        //핸들러
        handler = Handler()

        Thread{
            waitingRoomSocket!!.soTimeout = 2100000000
            clientReceiver = ClientReceiver(waitingRoomSocket!!)
            clientReceiver!!.start()
        }.start()

        //프래그먼트 커넥트
        fragmentWrapper = findViewById(R.id.fragment_wrapper)
        mainFragment = MainFragment()
        gameFragment = GameFragment()
        gameFragment!!.fetchSocket(waitingRoomSocket!!)    //소켓 전달
        feedFragment = FeedFragment()
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_wrapper, gameFragment!!, "게임하기")
        fragmentTransaction.add(R.id.fragment_wrapper, feedFragment!!, "소식")
        fragmentTransaction.add(R.id.fragment_wrapper, mainFragment!!, "홈")
        fragmentTransaction.commit()
        navigation = findViewById(R.id.navigation)
        navigation!!.setOnNavigationItemSelectedListener(this)
        navigation!!.setOnNavigationItemReselectedListener(this)
    }

    override fun onResume() {
        super.onResume()

        val intent = Intent(applicationContext, TaskFinished::class.java)

        startService(intent)
        /*
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d("서비스", "시작")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }, Context.BIND_AUTO_CREATE)
        */
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d("Main", "onDestroy Called!")
        waitingRoomSocket!!.close()
    }


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        switchFragment(p0.title.toString())
        Log.d("TITLE", p0.title.toString())
        return true
    }

    override fun onNavigationItemReselected(p0: MenuItem) {
        //TODO : 새로고침 코드
    }

    private fun switchFragment(tag : String) {
        val fragmentTransaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        for(t : String in tabList) {
            if(t == tag) fragmentTransaction.show(fragmentManager.findFragmentByTag(t)!!)
            else fragmentTransaction.hide(fragmentManager.findFragmentByTag(t)!!)
        }
        fragmentTransaction.commit()
    }




    //대기방은 3번이다
    private inner class ClientReceiver(socket : Socket) : Thread() {
        var din = DataInputStream(socket.getInputStream())
        override fun run() {
            Log.e("ClientReceiver 3", "Receiver Thread Started")
            while(true) {
                try {
                    val inString = din.readUTF()
                    if(inString == "ping") continue //핑 확인 메시지는 처리하지 않는다
                    val rData = inString.split(":;:")
                    val messageType = rData[0]
                    Log.i("ClientReceiver $messageType", inString)
                    when(rData[0]) {

                        "3-001" -> { //유저가 대기방에 접속했음을 알린 결과를 받는다. 방 리스트도 같이 받는다
                            //RECEIVED : 3-001:;:roomInfo:;:roomInfo:;:...
                            Log.i("WAITING ROOM", "유저 클라이언트가 대기방에 성공적으로 등록됨")
                            Log.e("대단해", inString)

                            //가져온 방 정보를 저장한다
                            for(i : Int in 1..(rData.lastIndex)) {
                                if(rData[i] == "") continue //비어있는 끝 칸 자르기
                                gameFragment!!.addRoomToRoomList(rData[i])
                            }
                        }
                        "3-002" -> { //


                        }
                        "3-003" -> { //단일 방의 생성, 추가, 삭제만 받는다
                            //RECEIVED: 3-003:;:type:;:different by type
                            //1:
                            //2:
                            //3:
                            gameFragment!!.onRoomStateChanged(rData[1].toInt(), rData[2])
                        }

                        "3-004" -> {
                            //RECEIVED : 3-004:;:roomId
                            handler!!.post {
                                Toast.makeText(applicationContext, "폐쇄된 방입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch(e1 : SocketException) {
                    Log.d("ClientReceiver 3", "SOCKET CLOSED");
                    return
                }
                catch(e : Exception) {
                    e.printStackTrace()
                    Log.e("ClientReceiver 3", "SERVER DISCONNECTED")
                }
            }
        }
    }
}