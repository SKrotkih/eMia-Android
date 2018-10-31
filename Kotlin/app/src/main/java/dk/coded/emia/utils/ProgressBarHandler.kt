package dk.coded.emia.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout

/**
 * Created by pa1pal on 12/6/17.
 */

class ProgressBarHandler(context: Context) {
    private val mProgressBar: ProgressBar

    init {
        val layout = (context as Activity).findViewById<View>(android.R.id.content)
                .rootView as ViewGroup

        mProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
        mProgressBar.isIndeterminate = true

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)

        val rl = RelativeLayout(context)

        rl.gravity = Gravity.CENTER
        rl.addView(mProgressBar)

        layout.addView(rl, params)

        hide()
    }

    fun show() {
        mProgressBar.visibility = View.VISIBLE
    }

    fun hide() {
        mProgressBar.visibility = View.INVISIBLE
    }
}
