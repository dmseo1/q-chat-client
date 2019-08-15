package com.dongmin.qchat.fragments

import com.dongmin.qchat.activities.Init.StaticData.userInfo
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.dongmin.qchat.R
import com.dongmin.qchat.adapters.VerPicNameGridCharactersAdapter
import com.dongmin.qchat.adapters.VerPicNameGridFriendsAdapter
import com.dongmin.qchat.elements.VerPicNameGridCharactersElement
import com.dongmin.qchat.elements.VerPicNameGridFriendsElement
import com.dongmin.qchat.library.HttpConnector
import com.dongmin.qchat.library.Statics
import com.dongmin.qchat.library.UIModifyAvailableListener
import org.json.JSONObject

class MainFragment : Fragment() {

    val TAG = "MainFragment"

    //loading view
    private var opaWindow : ImageView? = null
    private var pgBar : ProgressBar? = null


    //drawer
    private var menuDrawer : DrawerLayout? = null
    private var menuSelector : NavigationView? = null
    private var lHome : LinearLayout? = null
    private var lFriends : LinearLayout? = null
    private var lMessage : LinearLayout? = null
    private var lRanking : LinearLayout? = null
    private var lCharacterShop : LinearLayout? = null
    private var lSetting : LinearLayout? = null
    private var lLogout : LinearLayout? = null

    //HOME
    private var HOME : ConstraintLayout? = null
    private var lblNickname : TextView? = null
    private var lblPoints : TextView? = null
    private var lblExp : TextView? = null
    private var lblNoFriend : TextView? = null
    private var gridFriends : GridView? = null
    private var gridCharacters : GridView? = null
    private var gridFriendsAdapter = VerPicNameGridFriendsAdapter()
    private var gridCharactersAdapter = VerPicNameGridCharactersAdapter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val v = inflater.inflate(R.layout.fragment_main, container, false)

        /**
         * LOADING VIEW     */
        opaWindow = v.findViewById(R.id.opa_window)
        pgBar = v.findViewById(R.id.pg_bar)

        /**
         * DRAWER     */
        menuDrawer = v.findViewById(R.id.menu_drawer)
        menuSelector = v.findViewById(R.id.menu_selector)
        lHome = v.findViewById(R.id.l_home)
        lFriends = v.findViewById(R.id.l_friends)
        lMessage = v.findViewById(R.id.l_message)
        lRanking = v.findViewById(R.id.l_ranking)
        lCharacterShop = v.findViewById(R.id.l_character_shop)
        lSetting = v.findViewById(R.id.l_setting)
        lLogout = v.findViewById(R.id.l_logout)


        /**
        * HOME     */
        HOME = v.findViewById(R.id.HOME)
        lblNickname = v.findViewById(R.id.lbl_nickname)
        lblPoints = v.findViewById(R.id.lbl_points)
        lblExp = v.findViewById(R.id.lbl_exp)
        lblNoFriend = v.findViewById(R.id.HOME_friends_list_no_friend)
        gridFriends = v.findViewById(R.id.HOME_friends_list_grid)
        gridFriends!!.numColumns = 4
        gridCharacters = v.findViewById(R.id.HOME_characters_list_grid)
        gridCharacters!!.numColumns = 4

        lblNickname!!.text = userInfo.userNickname
        lblPoints!!.text = userInfo.userPoint.toString()
        lblExp!!.text = userInfo.userExp.toString()

        fillHomeLists()

        return v
    }

    private fun fillHomeLists() {
        var finishedPool = 0
        val numToFinish = 2

        Statics.loading(opaWindow, pgBar)
        HttpConnector("user/fetch_friends_list.php", "user_no=" + userInfo.userNo, object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {
                when{
                    result!!.contains("ERROR_CODE") ->  {
                        Toast.makeText(context, "친구 데이터를 가져오는 중 오류가 발생하였습니다. 관리자에게 문의하세요($result)", Toast.LENGTH_LONG).show()
                    }
                    result == "EMPTY_LIST" -> {
                        lblNoFriend!!.visibility = View.VISIBLE
                    }
                    else -> {
                        lblNoFriend!!.visibility = View.GONE
                        val ja = JSONObject(result).getJSONArray("friends_list")
                        for(i : Int in 0..(ja.length() - 1)) {
                            val element = VerPicNameGridFriendsElement()
                            element.fillFromJSON(ja.getJSONObject(i))
                            gridFriendsAdapter.addItem(element)
                        }
                        gridFriends!!.adapter = gridFriendsAdapter
                    }
                }
                if(++finishedPool == numToFinish) Statics.loaded(opaWindow, pgBar)
            }
        }).execute()

        HttpConnector("user/fetch_characters_list.php", "user_no=" + userInfo.userNo, object : UIModifyAvailableListener {
            override fun taskCompleted(result: String?) {
                when {
                    result!!.contains("ERROR_CODE") -> {
                        Toast.makeText(context, "캐릭터 데이터를 가져오는 중 오류가 발생하였습니다. 관리자에게 문의하세요($result)", Toast.LENGTH_LONG).show()
                    }
                    result == "EMPTY_LIST" -> {
                        Toast.makeText(context, "예기치 않은 오류가 발생하였습니다", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val ja = JSONObject(result).getJSONArray("characters_list")
                        for(i : Int in 0..(ja.length() - 1)) {
                            val element = VerPicNameGridCharactersElement()
                            element.fillFromJSON(ja.getJSONObject(i))
                            element.setNowUsing(userInfo.userNowUsingCharacter)
                            gridCharactersAdapter.addItem(element)
                        }
                        gridCharacters!!.adapter = gridCharactersAdapter
                    }
                }
                if(++finishedPool == numToFinish) Statics.loaded(opaWindow, pgBar)
            }
        }).execute()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        println("onActivityCreated")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("onAttach")
    }


    override fun onStart() {
        super.onStart()
        println("onStart")
    }

    override fun onResume() {
        super.onResume()
        println("MypageFragment: onResume")
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        println("onDetach")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("여기 호출 되는지좀 한번 봐봐")
        //outState.putString("user_nickname", lbl_user_nickname.getText().toString());
        //outState.putString("user_points", lbl_user_points.getText().toString());
        //outState.putString("user_exp", lbl_user_exp.getText().toString());
    }
}
