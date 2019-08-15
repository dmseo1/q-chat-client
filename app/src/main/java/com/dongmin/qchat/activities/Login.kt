package com.dongmin.qchat.activities

import com.dongmin.qchat.activities.Init.StaticData.userInfo
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import com.dongmin.qchat.R
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.UIModifyAvailableListener
import org.json.JSONObject

class Login : AppCompatActivity(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private var id: EditText? = null
    private var pw: EditText? = null
    private var login: Button? = null
    private var signUp: Button? = null
    private var autoLogin: CheckBox? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        id = findViewById(R.id.txt_id)
        pw = findViewById(R.id.txt_pw)
        login = findViewById(R.id.btn_login)
        login!!.setOnClickListener(this)
        signUp = findViewById(R.id.btn_signUp)
        signUp!!.setOnClickListener(this)
        autoLogin = findViewById(R.id.chk_auto)
        //autoLogin!!.setOnCheckedChangedListener(this)

    }

    override fun onClick(v : View) {
        when (v.id) {
            signUp!!.id -> {
                val intent = Intent(this@Login, SignUp::class.java)
                startActivity(intent)
            }
            login!!.id -> {
                val conn = HttpConnector("user/login.php", "user_id=" + id!!.text.toString() + "&user_pw=" + pw!!.text.toString() , object : UIModifyAvailableListener {
                    override fun taskCompleted(result : String?) {
                        when(result) {
                            "ERROR_CODE_1" -> Toast.makeText(this@Login, "아이디 또는 비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
                            "ERROR_CODE_2" -> Toast.makeText(this@Login, "로그인 서버에 문제가 발생하였습니다", Toast.LENGTH_SHORT).show()
                            else -> {
                                Toast.makeText(this@Login, "로그인 성공", Toast.LENGTH_SHORT).show()
                                userInfo.fillFromJSON(JSONObject(result).getJSONArray("user_info").getJSONObject(0))
                                startActivity(Intent(this@Login, Main::class.java))
                                finish()
                            }
                        }
                    }
                })
                conn.execute()
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when(checkedId) {
            autoLogin!!.id -> {
                if(autoLogin!!.isChecked) {
                    Toast.makeText(this@Login, "체크함", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Login, "체크해제함", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
