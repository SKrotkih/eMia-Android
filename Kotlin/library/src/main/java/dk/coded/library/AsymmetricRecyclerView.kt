package dk.coded.library

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver

class AsymmetricRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs), BVGAsymmetricView {
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

    override val columnWidth: Int
        get() = viewImpl.getColumnWidth(availableSpace)


    override val separatorHeight: Int
        get() = 0


    override val isAllowReordering: Boolean
        get() = viewImpl.isAllowReordering


    override val isDebugging: Boolean
        get() = viewImpl.isDebugging

    override val numColumns: Int
        get() = viewImpl.numColumns


    fun setRequestedColumnCount(requestedColumnCount: Int) {
        viewImpl.requestedColumnCount = requestedColumnCount
    }

    override val requestedHorizontalSpacing: Int
        get() = viewImpl.requestedHorizontalSpacing

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

    override fun fireOnItemClick(index: Int, v: View) {}

    override fun fireOnItemLongClick(index: Int, v: View): Boolean {
        return false
    }

    fun setRequestedHorizontalSpacing(spacing: Int) {
        viewImpl.requestedHorizontalSpacing = spacing
    }

    fun setDebugging(debugging: Boolean) {
        viewImpl.isDebugging = debugging
    }
}
