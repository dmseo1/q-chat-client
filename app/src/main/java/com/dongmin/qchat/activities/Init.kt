package com.dongmin.qchat.activities

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dongmin.qchat.R
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.UIModifyAvailableListener
import com.dongmin.qchat.elements.UserInfo
import org.json.JSONException
import org.json.JSONObject

class Init : AppCompatActivity() {

    //어플리케이션 기초 정보 static 변수
    companion object StaticData {
        var NO_PROFILE_IMG = ""
        var basicURL : String = "YOUR_ROOT_DIRECTORY";
        var basicIP : String = ""
        var minRoomMaxPeople : Int = 0
        var maxRoomMaxPeople : Int = 0
        var basicGamePort : Int = 0
        var basicUploadPort : Int = 0
        var user_setting : SharedPreferences? = null
        var userInfo : UserInfo = UserInfo()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)
        user_setting = getSharedPreferences("user_setting", MODE_PRIVATE)

        //어플리케이션 기본 정보를 로드
        HttpConnector("fetch_app_info.php", "", object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {

                //가져온 정보를 static 변수에 저장
                val data = JSONObject(result).getJSONObject("app_info")
                NO_PROFILE_IMG = data.getString("no_profile_img")
                basicURL = data.getString("basic_url")
                basicIP = data.getString("basic_ip")
                basicGamePort = data.getString("basic_game_port").toInt()
                basicUploadPort = data.getString("basic_upload_port").toInt()
                minRoomMaxPeople = data.getString("min_room_max_people").toInt()
                maxRoomMaxPeople = data.getString("max_room_max_people").toInt()

                //자동로그인 체크 여부에 따른 분기2
                val intent : Intent

                if(user_setting!!.getBoolean("is_auto", false)) {   //자동로그인 ON
                    intent = Intent(this@Init, Main::class.java)
                    //TODO : 유저 정보를 불러온 다음 로그인
                    user_setting!!.getInt("user_no", 0)
                    val conn = HttpConnector("user/fetch_user_info.php",
                        "user_no=" + user_setting!!.getInt("user_no", 0),

                        object: UIModifyAvailableListener {
                            override fun taskCompleted(result : String?) {
                                try {
                                    //유저 정보를 모두 받아와서 저장
                                    userInfo.fillFromJSON(JSONObject("result").getJSONArray("user_info").getJSONObject(0))
                                    startActivity(intent)
                                    finish()
                                } catch(e : JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    conn.execute()
                } else {                                                            //자동로그인 OFF
                    intent = Intent(this@Init, Login::class.java)    //로그인 페이지로 이동
                    startActivity(intent)
                    finish()
                }
            }
        }).execute()
    }
}

