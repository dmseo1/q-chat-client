package com.dongmin.qchat.elements

class ChattingRankingElement {
    var userNo : String = ""
    var ranking : String = ""
    var nickname : String = ""
    var points : String = ""
    var record : String = ""

    fun fill(ranking : Int, dataString : String) {
        val data = dataString.split("::")
        this.ranking = ranking.toString()
        this.userNo = data[0]
        this.nickname = data[1]
        this.points = data[2]
        this.record = data[3]
    }
}