package com.dongmin.qchat.library

/*
 * This file is part of Siebe Projects samples.
 *
 * Siebe Projects samples is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Siebe Projects samples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Siebe Projects samples.  If not, see <http://www.gnu.org/licenses/>.
 */



import com.dongmin.qchat.R
import android.app.Activity

import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

import android.view.WindowManager.LayoutParams

import android.widget.PopupWindow


/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed.
 */
class KeyboardHeightProvider
/**
 * Construct a new KeyboardHeightProvider
 *
 * @param activity The parent activity
 */
    (
    /** The root activity that uses this KeyboardHeightProvider  */
    private val activity: Activity
) : PopupWindow(activity) {

    /** The keyboard height observer  */
    private var observer: KeyboardHeightObserver? = null

    /** The cached landscape height of the keyboard  */
    private var keyboardLandscapeHeight: Int = 0

    /** The cached portrait height of the keyboard  */
    private var keyboardPortraitHeight: Int = 0

    /** The view that is used to calculate the keyboard height  */
    private val popupView: View?

    /** The parent view  */
    private val parentView: View

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    private val screenOrientation: Int
        get() = activity.resources.configuration.orientation

    init {

        val inflator = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.popupView = inflator.inflate(R.layout.etc_keyboard_height_determiner, null, false)
        contentView = popupView

        softInputMode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE or LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        parentView = activity.findViewById(android.R.id.content)

        width = 0
        height = LayoutParams.MATCH_PARENT

        popupView!!.viewTreeObserver.addOnGlobalLayoutListener {
            if (popupView != null) {
                handleOnGlobalLayout()
            }
        }
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun start() {

        if (!isShowing && parentView.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    fun close() {
        this.observer = null
        dismiss()
    }

    /**
     * Set the keyboard height observer to this provider. The
     * observer will be notified when the keyboard height has changed.
     * For example when the keyboard is opened or closed.
     *
     * @param observer The observer to be added to this provider.
     */
    fun setKeyboardHeightObserver(observer: KeyboardHeightObserver?) {
        this.observer = observer
    }

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity window height.
     */
    private fun handleOnGlobalLayout() {

        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)

        val rect = Rect()
        popupView!!.getWindowVisibleDisplayFrame(rect)

        // REMIND, you may like to change this using the fullscreen size of the phone
        // and also using the status bar and navigation bar heights of the phone to calculate
        // the keyboard height. But this worked fine on a Nexus.
        val orientation = screenOrientation
        val keyboardHeight = screenSize.y - rect.bottom

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation)
        } else {
            this.keyboardLandscapeHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation)
        }
    }

    /**
     *
     */
    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        if (observer != null) {
            observer!!.onKeyboardHeightChanged(height, orientation)
        }
    }

    companion object {

        /** The tag for logging purposes  */
        private val TAG = "sample_KeyboardHeightProvider"
    }
}
