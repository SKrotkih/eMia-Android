package dk.coded.library

import android.content.Context
import android.widget.LinearLayout

class LinearLayoutPoolObjectFactory(private val context: Context) : BasePoolObjectFactory<LinearLayout> {

    override fun createObject(): LinearLayout {
        return LinearLayout(context, null)
    }
}
