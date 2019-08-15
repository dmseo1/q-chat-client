package com.dongmin.qchat.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dongmin.qchat.R

class FeedFragment : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("a", "onCreateView")
        val v = inflater.inflate(R.layout.fragment_feed, container, false)


        return v
    }
}