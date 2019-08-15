package com.dongmin.qchat.elements

import com.dongmin.qchat.activities.Init.StaticData.basicURL
import org.json.JSONObject

class VerPicNameGridFriendsElement : IFillFromJSON {

    var no : Long = 0
    var nickname : String = ""
    var path : String = ""

    override fun fillFromJSON(data: JSONObject) {
        no = data.getString("user_no").toLong()
        nickname = data.getString("user_nickname")
        path = data.getString("user_profile_img_path_t")
    }

}