package com.dongmin.qchat.library

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent


/**
 * Created by seodongmin on 2017-08-22.
 */

class BackPressEditText : android.support.v7.widget.AppCompatEditText {
    private var _listener: OnBackPressListener? = null


    constructor(context: Context) : super(context) {}


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}


    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && _listener != null) {
            _listener!!.onBackPress()
        }

        return super.onKeyPreIme(keyCode, event)
    }


    fun setOnBackPressListener(`$listener`: OnBackPressListener) {
        _listener = `$listener`
    }

    interface OnBackPressListener {
        fun onBackPress()
    }
}