package com.dongmin.qchat.popups

import kotlinx.android.synthetic.main.popup_room_create.*
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.*
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.ChattingRoom
import com.dongmin.qchat.activities.Init.StaticData.maxRoomMaxPeople
import com.dongmin.qchat.activities.Init.StaticData.minRoomMaxPeople
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.Statics
import com.dongmin.qchat.library.UIModifyAvailableListener
import org.json.JSONObject


class RoomCreate(context : Context) : View.OnClickListener {

    private var backButtonChance = false
    private var backButtonDup = false

    private var dlg = Dialog(context)
    private var dContext = dlg.context
    private var txtRoomTitle : EditText? = null
    private var spnMaxPeople : Spinner? = null
    private var spnQType : Spinner? = null
    private var spnMaxPeopleAdapter : ArrayAdapter<String>? = null
    private var spnQTypeAdapter : ArrayAdapter<String>? = null
    private var rdPTypePublic : RadioButton? = null
    private var rdPTypePrivate : RadioButton? = null
    private var txtRoomPassword : EditText? = null
    private var btnCreate : Button? = null
    private var btnCancel : Button? = null
    private var opaWindow : ImageView? = null
    private var pgBar : ProgressBar? = null


    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.popup_room_create)
        dlg.setOnKeyListener { _, keyCode, _ ->
            if(keyCode == KeyEvent.KEYCODE_BACK){
                if(backButtonDup) return@setOnKeyListener false
                backButtonDup = true
                Thread {
                    Thread.sleep(500)
                    backButtonDup = false
                }.start()

                when(backButtonChance) {
                    true -> {
                        dlg.dismiss()
                    }
                    false -> {
                        Toast.makeText(dContext, "백버튼을 한 번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show()
                        backButtonChance = true
                        Thread {
                            Thread.sleep(2000)
                            backButtonChance = false
                        }.start()
                    }
                }
            }
            return@setOnKeyListener false
        }
        dlg.setCancelable(false)
        dlg.show()

        txtRoomTitle = dlg.findViewById(R.id.txt_room_title)
        spnMaxPeople = dlg.findViewById(R.id.spn_max_people)
        spnQType = dlg.findViewById(R.id.spn_q_type)
        spnMaxPeopleAdapter
        rdPTypePublic = dlg.findViewById(R.id.rd_p_type_public)
        rdPTypePrivate = dlg.findViewById(R.id.rd_p_type_private)
        txtRoomPassword = dlg.findViewById(R.id.txt_room_password)
        btnCreate = dlg.findViewById(R.id.btn_create)

        btnCancel = dlg.findViewById(R.id.btn_cancel)
        btnCancel!!.setOnClickListener(this)
        opaWindow = dlg.findViewById(R.id.opa_window)
        pgBar = dlg.findViewById(R.id.pg_bar)

        //스피너 채우기
        fillMaxPeople()
        fillQTypeAndTitlePreset()

    }

    override fun onClick(v: View?) {
        when(v!!) {
            btnCreate -> {
                val intent = Intent(dContext, ChattingRoom::class.java)
                intent.putExtra("title", if(txtRoomTitle!!.text.toString() == "") txtRoomTitle!!.hint.toString() else txtRoomTitle!!.text.toString())
                intent.putExtra("p_type", if(rdPTypePublic!!.isChecked) "public" else "private")
                intent.putExtra("q_type", spnQType!!.selectedItem.toString())
                intent.putExtra("password", txtRoomPassword!!.text.toString())
                intent.putExtra("current_people", "1")
                intent.putExtra("max_people", spnMaxPeople!!.selectedItem.toString())
                intent.putExtra("is_room_creator", true)
                dlg.context.startActivity(intent)
                dlg.dismiss()
            }
            btnCancel -> {
                dlg.dismiss()
            }
            rdPTypePublic -> {

            }
            rdPTypePrivate -> {

            }
        }
    }

    private fun fillQTypeAndTitlePreset() {
        Statics.loading(opaWindow, pgBar)
        var finishedPool = 0
        val numToFinish = 2

        HttpConnector("game/fetch_title_preset.php", "", object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {
                when(result) {
                    "ERROR_CODE_0", "ERROR_CODE_1" -> Statics.errorToastMessage(dContext, result)
                    else -> {
                        txtRoomTitle!!.hint = result
                    }
                }
                if(++finishedPool == numToFinish) Statics.loaded(opaWindow, pgBar)
            }
        }).execute()

        HttpConnector("game/fetch_q_type.php", "", object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {
                when(result) {
                    "ERROR_CODE_0", "ERROR_CODE_1" -> Statics.errorToastMessage(dContext, result)
                    else -> {
                        val spnQTypeList = ArrayList<String>()
                        val data = JSONObject(result).getJSONArray("q_type")
                        for(i : Int in  0..(data.length() - 1)) {
                            spnQTypeList.add(data[i].toString())
                        }
                        spnQTypeAdapter = ArrayAdapter(dContext, R.layout.support_simple_spinner_dropdown_item, spnQTypeList)
                        spnQType!!.adapter = spnQTypeAdapter
                    }
                }
                if(++finishedPool == numToFinish) {
                    Statics.loaded(opaWindow, pgBar)
                    activateCreateButton()  //스피너에 정보가 모두 채워지고 난 뒤에 방 생성 버튼을 활성화하도록 한다
                }
            }
        }).execute()
    }

    private fun activateCreateButton() {
        btnCreate!!.setOnClickListener(this)
    }

    private fun fillMaxPeople() {
        val maxPeopleList = ArrayList<String>()
        for(i : Int in minRoomMaxPeople..maxRoomMaxPeople) {
            maxPeopleList.add(i.toString())
        }
        spnMaxPeopleAdapter = ArrayAdapter(dContext, R.layout.support_simple_spinner_dropdown_item, maxPeopleList)
        spnMaxPeople!!.adapter = spnMaxPeopleAdapter
    }
}