package com.dongmin.qchat.library

import com.dongmin.qchat.activities.Init.StaticData.basicURL
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class HttpConnector constructor(private val path : String, private val param : String, private val listener : UIModifyAvailableListener?) : AsyncTask<Void, Int, String>() {

    override fun doInBackground(vararg params: Void?): String {
        var result = ""
        Log.d(path, param)
        try {
            val url = URL(basicURL + this.path)
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.requestMethod = "POST"
            conn.doInput = true
            try {
                conn.connect()
            } catch(e : ConnectException) {
                e.printStackTrace()
            }

            //안드로이드 -> 서버
            val outs = conn.outputStream
            outs.write(this.param.toByteArray())
            //Log.d("REAL_PARAM", this.param.toByteArray(charset("UTF-8")).toString())
            outs.flush()
            outs.close()

            //서버 -> 안드로이드
            val inputStream: InputStream?
            val bufferedReader: BufferedReader?
            val data : String

            inputStream = conn.inputStream
            bufferedReader = BufferedReader(InputStreamReader(inputStream!!), 8 * 1024)
            var line : String?
            val buff = StringBuffer()
           // if(bufferedReader.readLine() == null) Log.d("wow", "wow")
            while (true) {
                line =  bufferedReader.readLine()
                if(line == null) break
                else buff.append(line + "\n")
            }

            data = buff.toString().trim { it <= ' ' }
            Log.e("RECV DATA", data)
            result = data

        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        this.listener!!.taskCompleted(result)
    }
}