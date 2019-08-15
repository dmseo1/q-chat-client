package com.dongmin.qchat.elements

import org.json.JSONObject


class ChattingRoomElement : IFillFromJSON{

    var title: String = ""
    var currentPeople: String = ""  //현재 인원수
    var maxPeople: String = ""      //최대 인원수
    var pType: String = ""          //공개 타입
    var qType: String = ""          //퀴즈 타입
    var cTime: String = ""          //방 생성 시각. 방을 구분하는 키 중 하나로 쓰인다.
    var standardTime : String = ""  //퀴즈 시간 동기화의 기준 숫자. 방 생성 당시의 서버의 System.nanoTime()을 가져오므로, 방의 고유 불변값이다.
    var creatorUserNo : String = "" //방 생성자의 유저번호. 방을 구분하는 키 중 하나로 쓰인다. cTime 과 creatorUserNo 를 합하여 방을 구분하는 roomId 로 사용한다.
    var roomId : String = ""
    var currentQuestion: String = ""
    var currentQuestionText: String = ""
    var currentAnswer: String = ""
    var fetchqCode: String = ""
    var getStime: String = ""
    var password: String = ""

    override fun fillFromJSON(data: JSONObject) {
        roomId = data.getString("room_id")
        password = data.getString("password")
        currentPeople = data.getString("current_people")
        maxPeople = data.getString("max_people")
        cTime = data.getString("c_time")
        pType = data.getString("p_type")
        qType = data.getString("q_type")
        title = data.getString("title")
    }
}