package dk.coded.library

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver

class AsymmetricRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs), AsymmetricView {
    private val viewImpl: AsymmetricViewImpl
    private var adapter: AsymmetricRecyclerViewAdapter<*>? = null

    private val availableSpace: Int
        get() = measuredWidth - paddingLeft - paddingRight

    init {
        viewImpl = AsymmetricViewImpl(context)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val vto = viewTreeObserver
        vto?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                viewTreeObserver.removeGlobalOnLayoutListener(this)
                viewImpl.determineColumns(availableSpace)
                if (adapter != null) {
                    adapter!!.recalculateItemsPerRow()
                }
            }
        })
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        if (adapter !is AsymmetricRecyclerViewAdapter<*>) {
            throw UnsupportedOperationException(
                    "Adapter must be an instance of AsymmetricRecyclerViewAdapter")
        }

        this.adapter = adapter
        super.setAdapter(adapter)

        this.adapter!!.recalculateItemsPerRow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewImpl.determineColumns(availableSpace)
    }

    override fun isDebugging(): Boolean {
        return viewImpl.isDebugging
    }

    override fun getNumColumns(): Int {
        return viewImpl.numColumns
    }

    override fun isAllowReordering(): Boolean {
        return viewImpl.isAllowReordering
    }

    override fun fireOnItemClick(index: Int, v: View) {}

    override fun fireOnItemLongClick(index: Int, v: View): Boolean {
        return false
    }

    override fun getColumnWidth(): Int {
        return viewImpl.getColumnWidth(availableSpace)
    }

    override fun getDividerHeight(): Int {
        return 0
    }

    override fun getRequestedHorizontalSpacing(): Int {
        return viewImpl.requestedHorizontalSpacing
    }

    fun setRequestedColumnCount(requestedColumnCount: Int) {
        viewImpl.setRequestedColumnCount(requestedColumnCount)
    }

    fun setRequestedHorizontalSpacing(spacing: Int) {
        viewImpl.requestedHorizontalSpacing = spacing
    }

    fun setDebugging(debugging: Boolean) {
        viewImpl.isDebugging = debugging
    }
}
