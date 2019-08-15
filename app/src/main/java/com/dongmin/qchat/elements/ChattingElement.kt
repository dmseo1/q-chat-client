package com.dongmin.qchat.elements

class ChattingElement () {
    var category : String = "1"
    var userNo : String = ""
    var userNickname : String = ""
    var message : String = ""

    constructor(category : String, userNo : String, userNickname : String, message : String) : this() {
        this.category = category
        this.userNo = userNo
        this.userNickname = userNickname
        this.message = message
    }

    fun fill(category : String, userNo : String, userNickname : String, message : String) {
        this.category = category
        this.userNo = userNo
        this.userNickname = userNickname
        this.message = message
    }

    fun fill(category : String, message : String) {
        this.category = category
        this.message = message
    }
}