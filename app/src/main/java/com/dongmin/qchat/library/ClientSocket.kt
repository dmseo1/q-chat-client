package com.dongmin.qchat.library

import java.io.DataOutputStream
import java.net.Socket

class ClientSocket(host : String, ip : Int) : Socket(host, ip) {

    override fun close() {
        Thread {
            val out = DataOutputStream(this.outputStream)
            out.writeUTF("ACK_SOCKET_CLOSED")
            super.close()
        }.start()
    }
}