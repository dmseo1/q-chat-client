package com.dongmin.qchat.library

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.dongmin.qchat.R

class Statics {
    companion object StaticFunc {
        fun loading(opaWindow : View?, pgBar : ProgressBar?) {
            opaWindow!!.visibility = View.VISIBLE
            pgBar!!.visibility = View.VISIBLE
        }

        fun loaded(opaWindow : View?, pgBar : ProgressBar?) {
            opaWindow!!.visibility = View.GONE
            pgBar!!.visibility = View.GONE
        }

        fun errorToastMessage(context : Context, code : String) {
            Toast.makeText(context, "서버에 문제가 발생하였습니다. 관리자에게 문의하세요($code)", Toast.LENGTH_LONG).show()
        }

    }
}