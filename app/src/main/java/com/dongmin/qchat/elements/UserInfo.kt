package com.dongmin.qchat.elements

import android.graphics.drawable.Drawable
import org.json.JSONObject

class UserInfo : IFillFromJSON {
    //기초 정보
    var userNo :Long = 0
    var userId : String = ""
    var userNickname : String = ""
    var userEmail : String = ""
    var userStatusMessage : String = ""
    var userPoint : Long = 0
    var userExp : Long = 0
    var userNowUsingCharacter : Int = 0

    //프로필 이미지 관련
    var userProfileImg : Drawable? = null
    var userProfileImgT : Drawable? = null
    var userProfileImgPath : String = ""
    var userProfileImgPathT : String =  ""

    //리스트
    var userMessageBlockList : String = ""
    var userFriendsList : String = ""
    var userCharactersList : String = ""


    override fun fillFromJSON(data : JSONObject) {
        this.userNo = data.getString("user_no").toLong()
        this.userId = data.getString("user_id")
        this.userEmail = data.getString("user_email")
        this.userNickname = data.getString("user_nickname")
        this.userStatusMessage = data.getString("user_status_message")
        this.userPoint = data.getLong("user_point")
        this.userExp = data.getLong("user_exp")
        this.userNowUsingCharacter = data.getInt("user_now_using_character")
        this.userProfileImgPath = data.getString("user_profile_img_path")
        this.userProfileImgPathT = data.getString("user_profile_img_path_t")
        this.userMessageBlockList = data.getString("user_message_block_list")
        this.userFriendsList = data.getString("user_friends_list")
        this.userCharactersList = data.getString("user_characters_list")
    }

}

