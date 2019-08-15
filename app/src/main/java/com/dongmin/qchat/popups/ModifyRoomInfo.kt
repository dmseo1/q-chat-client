package com.dongmin.qchat.popups

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.*
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init
import com.dongmin.qchat.elements.ChattingRoomElement
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.Statics
import com.dongmin.qchat.library.UIModifyAvailableListener
import org.json.JSONObject

class ModifyRoomInfo(context : Context, private val roomInfo : ChattingRoomElement) : View.OnClickListener {

    private var type = 0
    private var isModified = false

    private var dlg = Dialog(context)
    private var dContext = dlg.context

    //주요 뷰
    private lateinit var lblTitle : TextView
    private lateinit var txtTitle : EditText
    private lateinit var spnQTypeOrMaxPeople : Spinner
    private lateinit var spnQTypeOrMaxPeopleAdapter : ArrayAdapter<String>
    private lateinit var lPType : LinearLayout
    private lateinit var rdPTypePublic : RadioButton
    private lateinit var rdPTypePrivate : RadioButton
    private lateinit var txtRoomPassword : EditText

    //로딩
    private lateinit var pgBar : ProgressBar
    private lateinit var opaWindow : ImageView

    //하단 버튼
    private lateinit var btnCancel : Button
    private lateinit var btnOK : Button


    //소켓 전송을 위한 콜백 인터페이스
    private lateinit var callback : Callback
    interface Callback {
        fun sendToServer(messageType : String, message : String)
        fun emptyRoom()
    }

    //type: 제목: 1, 문제타입: 2, 공개타입: 3, 최대인원수: 4
    fun start(type : Int) {
        this.type = type

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.popup_modify_room_info)

        //로딩
        pgBar = dlg.findViewById(R.id.pg_bar)
        opaWindow = dlg.findViewById(R.id.opa_window)

        //하단 버튼
        btnCancel = dlg.findViewById(R.id.btn_cancel)
        btnOK = dlg.findViewById(R.id.btn_ok)
        btnCancel.setOnClickListener(this)
        btnOK.setOnClickListener(this)

        //어떤 것을 수정하느냐에 따라 다른 뷰 처리
        lblTitle = dlg.findViewById(R.id.lbl_title)

        when(type) {
            1 -> {
                lblTitle.text = "방 제목 수정"
                txtTitle = dlg.findViewById(R.id.txt_title)
                txtTitle.setText(roomInfo.title)
                txtTitle.visibility = View.VISIBLE
            }

            2 -> {
                lblTitle.text = "문제 타입 수정"
                spnQTypeOrMaxPeople = dlg.findViewById(R.id.spn_q_type_or_max_people)
                spnQTypeOrMaxPeople.visibility = View.VISIBLE
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
                                spnQTypeOrMaxPeopleAdapter = ArrayAdapter(dContext, R.layout.support_simple_spinner_dropdown_item, spnQTypeList)
                                spnQTypeOrMaxPeople.adapter = spnQTypeOrMaxPeopleAdapter

                                for(i : Int in 0..(data.length() - 1)) {
                                    if(spnQTypeOrMaxPeople.selectedItem.toString() == roomInfo.qType) {
                                        spnQTypeOrMaxPeople.setSelection(i)
                                        break
                                    }
                                }

                            }
                        }
                    }
                }).execute()
            }

            3 -> {

                lblTitle.text = "공개 타입 수정"
                lPType = dlg.findViewById(R.id.l_p_type)

                rdPTypePublic = dlg.findViewById(R.id.rd_p_type_public)
                rdPTypePublic.setOnCheckedChangeListener { _, _ ->
                    if(rdPTypePublic.isChecked) {
                        txtRoomPassword.visibility = View.GONE
                    }
                }

                rdPTypePrivate = dlg.findViewById(R.id.rd_p_type_private)
                rdPTypePrivate.setOnCheckedChangeListener{ _, _ ->
                    if(rdPTypePrivate.isChecked) {
                        txtRoomPassword.visibility = View.VISIBLE
                    }
                }

                txtRoomPassword = dlg.findViewById(R.id.txt_room_password)
                txtRoomPassword.setText(roomInfo.password)
                lPType.visibility = View.VISIBLE

                when(roomInfo.pType) {
                    "public" -> {
                        rdPTypePublic.isChecked = true
                        txtRoomPassword.visibility = View.GONE
                    }
                    "private" -> {
                        rdPTypePrivate.isChecked = true
                        txtRoomPassword.visibility = View.VISIBLE
                    }
                }
            }

            4 -> {
                lblTitle.text = "최대 인원수 수정"
                spnQTypeOrMaxPeople = dlg.findViewById(R.id.spn_q_type_or_max_people)
                spnQTypeOrMaxPeople.visibility = View.VISIBLE
                val maxPeopleList = ArrayList<String>()
                for(i : Int in Init.minRoomMaxPeople..Init.maxRoomMaxPeople) {
                    maxPeopleList.add(i.toString())
                }
                spnQTypeOrMaxPeopleAdapter = ArrayAdapter(dContext, R.layout.support_simple_spinner_dropdown_item, maxPeopleList)
                spnQTypeOrMaxPeople.adapter = spnQTypeOrMaxPeopleAdapter
                spnQTypeOrMaxPeople.setSelection(roomInfo.maxPeople.toInt() - 4)
            }
        }
        //dlg.setCancelable(false)
        dlg.show()

    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            btnCancel.id -> {
                dlg.dismiss()
            }

            btnOK.id -> {
                var modifiedContent = ""
                when(this.type) {
                    1 -> if(this.roomInfo.title != txtTitle.text.toString()) {
                        isModified = true
                        if(txtTitle.text.toString().trim() == "") {
                            Toast.makeText(dContext, "제목을 공백 제외 한 글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                            return
                        }
                        modifiedContent = txtTitle.text.toString()
                    }
                    2 -> if(this.roomInfo.qType != spnQTypeOrMaxPeople.selectedItem.toString()) {
                        isModified = true
                        modifiedContent = spnQTypeOrMaxPeople.selectedItem.toString()
                    }
                    3 -> if(this.roomInfo.pType != when(rdPTypePublic.isChecked) { true -> "public"; false -> "private"}) {
                        isModified = true

                        if(txtRoomPassword.text.toString() == "") {
                            Toast.makeText(dContext, "비공개방으로 전환시 반드시 비밀번호를 입력하여야 합니다.", Toast.LENGTH_SHORT).show()
                            return
                        } else if(txtRoomPassword.text.toString().trim() == "") {
                            Toast.makeText(dContext, "비밀번호를 공백 제외 한 글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        modifiedContent = when(rdPTypePublic.isChecked) { true -> "public"; false -> "private"}
                        if(modifiedContent == "private") {
                            modifiedContent += ":;:${txtRoomPassword.text}"
                        }
                    }
                    4 -> if(this.roomInfo.maxPeople != spnQTypeOrMaxPeople.selectedItem.toString()) {
                        isModified = true
                        modifiedContent = spnQTypeOrMaxPeople.selectedItem.toString()
                    }
                }

                if(isModified) callback.sendToServer("1-009", "${roomInfo.roomId}:;:$type:;:$modifiedContent")

                dlg.dismiss()
                Toast.makeText(dContext, "방 정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
            }

            rdPTypePublic.id -> {
                txtRoomPassword.visibility = View.GONE
            }

            rdPTypePrivate.id -> {
                txtRoomPassword.visibility = View.VISIBLE
            }
        }
    }

    fun setCallback(callback : Callback) {
        this.callback = callback
    }

}