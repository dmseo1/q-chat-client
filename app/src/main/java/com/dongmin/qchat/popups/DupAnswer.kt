package com.dongmin.qchat.popups

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.dongmin.qchat.R

class DupAnswer(context : Context) : View.OnClickListener{

    private val dlg = Dialog(context)
    private var content : TextView? = null
    private var ok : Button? = null


    fun start(message : String) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.popup_dup_answer)
        dlg.setCancelable(false)
        dlg.show()

        content = dlg.findViewById(R.id.content)
        content!!.text = message
        ok = dlg.findViewById(R.id.ok)
        ok!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            ok!!.id -> {
                dlg.dismiss()
            }
        }
    }
}
