package dk.coded.emia.View

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpacesItemDecoration(private val padding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.bottom = padding
    }
}