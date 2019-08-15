package com.dongmin.qchat.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.dongmin.qchat.activities.Main.Companion.chattingRoomSocket
import com.dongmin.qchat.activities.Main.Companion.waitingRoomSocket
import java.io.DataOutputStream
import java.net.Socket


class TaskFinished : Service() {

    private val TAG = "TaskFinished"
    var isWaitingRoomSocketFetched = false
    var isChattingRoomSocketFetched = false


    override fun onCreate() {
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved")
        Thread {
            try {
                DataOutputStream(waitingRoomSocket!!.outputStream).writeUTF("ACK_SOCKET_CLOSED")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                DataOutputStream(chattingRoomSocket!!.outputStream).writeUTF("ACK_SOCKET_CLOSED")
            } catch(e : Exception) {
                e.printStackTrace()
            }
        }.start()
        super.onTaskRemoved(rootIntent)
        stopService(rootIntent)
    }
}