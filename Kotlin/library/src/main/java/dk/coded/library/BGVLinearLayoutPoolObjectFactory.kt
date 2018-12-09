package dk.coded.library

import android.content.Context
import android.widget.LinearLayout

class BGVLinearLayoutPoolObjectFactory(private val context: Context) : BGVPoolObjectFactory<LinearLayout> {

    override fun createObject(): LinearLayout {
        return LinearLayout(context, null)
    }
}
