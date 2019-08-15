package com.dongmin.qchat.elements

import org.json.JSONObject

class VerPicNameGridCharactersElement : IFillFromJSON{

    var no : Int = 0
    var name : String = ""
    var imagePath : String = ""
    var nowUsing : Boolean = false

    fun setNowUsing(nowUsingCharacter : Int) {
        nowUsing = (no == nowUsingCharacter)
    }

    override fun fillFromJSON(data : JSONObject) {
        no = data.getString("character_no").toInt()
        name = data.getString("character_name")
        imagePath = data.getString("path")
    }
}