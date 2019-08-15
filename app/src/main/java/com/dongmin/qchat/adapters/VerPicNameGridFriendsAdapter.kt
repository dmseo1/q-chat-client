package com.dongmin.qchat.adapters

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.dongmin.qchat.R
import com.dongmin.qchat.activities.Init.StaticData.NO_PROFILE_IMG
import com.dongmin.qchat.elements.VerPicNameGridFriendsElement

import java.util.ArrayList

/**
 * Created by seodongmin on 2017-08-02.
 */

class VerPicNameGridFriendsAdapter : BaseAdapter() {

    private val listItem: ArrayList<VerPicNameGridFriendsElement> = ArrayList()

    override fun getCount(): Int {
        return listItem.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cV = convertView
        val context = parent.context

        // Layout을 inflate하여 convertView 참조 획득.
        if (cV == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cV = inflater.inflate(R.layout.element_ver_pic_name_grid, parent, false)
        }

        val image = cV!!.findViewById(R.id.img_image) as ImageView
        val name = cV.findViewById(R.id.description) as TextView

        //아이템 내 각 위젯에 데이터 반영
        //이미지는 스레드 처리
        val mHandler = Handler()
        Thread(Runnable {
            mHandler.post {
                //유저 섬네일 이미지
                if(listItem[position].path == NO_PROFILE_IMG ) {
                    image.setImageResource(R.drawable.ico_no_profile_img)
                } else {
                    Glide.with(context)
                        .load("http://dak2183242.cafe24.com/qchat/user/img_profile/thumbnail/" + listItem[position].path)
                        .into(image)
                }

            }
        }).start()
        name.text = listItem[position].nickname

        return cV
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    override fun getItem(position: Int): Any {
        return listItem[position]
    }

    fun addItem(element: VerPicNameGridFriendsElement) {
        listItem.add(0, element)
    }
}
