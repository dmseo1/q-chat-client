package com.dongmin.qchat.popups

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init
import com.dongmin.qchat.elements.UserInfo
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.UIModifyAvailableListener
import org.json.JSONObject

class MemberInfo(val context : Context) : View.OnClickListener {

    private var dlg = Dialog(context)

    //버튼
    private lateinit var btnClose : ImageView
    private lateinit var btnSendJjokji : ImageView
    private lateinit var btnHandleFriend : ImageView

    //레이블
    private lateinit var lblNickname : TextView
    private lateinit var lblUserId : TextView
    private lateinit var lblStatusMessage : TextView


    //프로필 사진 뷰
    private lateinit var imgProfileImg : ImageView

    fun start(userNo : Long) {

        btnClose = dlg.findViewById(R.id.btn_close)
        btnSendJjokji = dlg.findViewById(R.id.btn_send_jjokji)
        btnHandleFriend = dlg.findViewById(R.id.btn_handle_friend)

        lblNickname = dlg.findViewById(R.id.lbl_nickname)
        lblUserId = dlg.findViewById(R.id.lbl_user_id)
        lblStatusMessage = dlg.findViewById(R.id.lbl_status_message)

        imgProfileImg = dlg.findViewById(R.id.img_profile_img)


        HttpConnector("user/fetch_user_info.php", "userNo=$userNo", object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {
                val loadedUserInfo = UserInfo()
                loadedUserInfo.fillFromJSON(JSONObject(result))
                lblNickname.text = loadedUserInfo.userNickname
                lblUserId.text = context.resources.getString(R.string.popup_member_info_user_id, loadedUserInfo.userId)
                lblStatusMessage.text = loadedUserInfo.userStatusMessage
                Glide.with(context).load("${Init.basicURL}/user/img_profile/${loadedUserInfo.userProfileImgPath}").into(imgProfileImg)
            }
        }).execute()

        dlg.show()
    }


    override fun onClick(v: View?) {
        when(v!!.id) {
            btnClose.id -> {
                dlg.dismiss()
            }
        }
    }

}