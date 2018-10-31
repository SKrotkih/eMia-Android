package dk.coded.library

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView

class AsymmetricGridView(context: Context, attrs: AttributeSet) : ListView(context, attrs), AsymmetricView {
    protected var onItemClickListener: AdapterView.OnItemClickListener? = null
    protected var onItemLongClickListener: AdapterView.OnItemLongClickListener? = null
    protected var gridAdapter: AsymmetricGridViewAdapter? = null
    private val viewImpl: AsymmetricViewImpl

    private val availableSpace: Int
        get() = measuredWidth - paddingLeft - paddingRight

    init {

        viewImpl = AsymmetricViewImpl(context)

        val vto = viewTreeObserver
        vto?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                viewTreeObserver.removeGlobalOnLayoutListener(this)
                viewImpl.determineColumns(availableSpace)
                if (gridAdapter != null) {
                    gridAdapter!!.recalculateItemsPerRow()
                }
            }
        })
    }

    override fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        onItemClickListener = listener
    }

    override fun fireOnItemClick(position: Int, v: View) {
        if (onItemClickListener != null) {
            onItemClickListener!!.onItemClick(this, v, position, v.id.toLong())
        }
    }

    override fun setOnItemLongClickListener(listener: AdapterView.OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    override fun fireOnItemLongClick(position: Int, v: View): Boolean {
        return onItemLongClickListener != null && onItemLongClickListener!!
                .onItemLongClick(this, v, position, v.id.toLong())
    }

    override fun setAdapter(adapter: ListAdapter) {
        if (adapter !is AsymmetricGridViewAdapter) {
            throw UnsupportedOperationException(
                    "Adapter must be an instance of AsymmetricGridViewAdapter")
        }

        gridAdapter = adapter
        super.setAdapter(adapter)

        gridAdapter!!.recalculateItemsPerRow()
    }

    fun setRequestedColumnWidth(width: Int) {
        viewImpl.setRequestedColumnWidth(width)
    }

    fun setRequestedColumnCount(requestedColumnCount: Int) {
        viewImpl.setRequestedColumnCount(requestedColumnCount)
    }

    override fun getRequestedHorizontalSpacing(): Int {
        return viewImpl.requestedHorizontalSpacing
    }

    fun setRequestedHorizontalSpacing(spacing: Int) {
        viewImpl.requestedHorizontalSpacing = spacing
    }

    fun determineColumns() {
        viewImpl.determineColumns(availableSpace)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewImpl.determineColumns(availableSpace)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return viewImpl.onSaveInstanceState(superState)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is AsymmetricViewImpl.SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        viewImpl.onRestoreInstanceState(state)

        setSelectionFromTop(20, 0)
    }

    override fun getNumColumns(): Int {
        return viewImpl.numColumns
    }

    override fun getColumnWidth(): Int {
        return viewImpl.getColumnWidth(availableSpace)
    }

    override fun isAllowReordering(): Boolean {
        return viewImpl.isAllowReordering
    }

    fun setAllowReordering(allowReordering: Boolean) {
        viewImpl.isAllowReordering = allowReordering
        if (gridAdapter != null) {
            gridAdapter!!.recalculateItemsPerRow()
        }
    }

    override fun isDebugging(): Boolean {
        return viewImpl.isDebugging
    }

    fun setDebugging(debugging: Boolean) {
        viewImpl.isDebugging = debugging
    }
}
