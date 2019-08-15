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
import com.dongmin.qchat.elements.VerPicNameGridCharactersElement

import java.util.ArrayList

/**
 * Created by seodongmin on 2017-08-02.
 */

class VerPicNameGridCharactersAdapter : BaseAdapter() {

    private val listItem: ArrayList<VerPicNameGridCharactersElement> = ArrayList()

    override fun getCount(): Int {
        return listItem.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cV = convertView
        val context = parent.context

        // Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cV = inflater.inflate(R.layout.element_ver_pic_name_grid, parent, false)
        }

        val face = cV!!.findViewById(R.id.img_face) as ImageView
        val image = cV.findViewById(R.id.img_image) as ImageView
        val description = cV.findViewById(R.id.description) as TextView
        val nowUsing = cV.findViewById(R.id.lbl_now_using) as TextView

        //이미지는 스레드 처리
        val mHandler = Handler()
        Thread(Runnable {
            mHandler.post {
                //캐릭터 이미지 로딩
                Glide.with(context)
                    .load("http://dak2183242.cafe24.com/qchat/character/" + listItem[position].imagePath)
                    .into(image)
                //사용자 얼굴 로딩
                //Glide.with(context)
                //    .load("http://dak2183242.cafe24.com/qchat/user/img_character_face/" + listItem[position].facePath)
                //    .into(face)
            }
        }).start()

        //캐릭터명 및 사용중 태그 표시
        description.text = listItem[position].name
        if(listItem[position].nowUsing) nowUsing.visibility = View.VISIBLE
        else nowUsing.visibility = View.GONE

        return cV
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    override fun getItem(position: Int): Any {
        return listItem[position]
    }

    fun addItem(element: VerPicNameGridCharactersElement) {
        listItem.add(element)
    }
}
