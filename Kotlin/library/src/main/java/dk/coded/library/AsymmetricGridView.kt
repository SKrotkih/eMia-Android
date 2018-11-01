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

    protected var _onItemClickListener: AdapterView.OnItemClickListener? = null
    protected var _onItemLongClickListener: AdapterView.OnItemLongClickListener? = null
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

    override var isDebugging: Boolean
        get() = viewImpl.isDebugging
        set(value) {viewImpl.isDebugging = value}

    override val numColumns: Int
        get() = viewImpl.numColumns

    override var isAllowReordering: Boolean
        get() = viewImpl.isAllowReordering
        set(value) {
            viewImpl.isAllowReordering = value
            if (gridAdapter != null) {
                gridAdapter!!.recalculateItemsPerRow()
            }
        }

    override val columnWidth: Int
        get() = viewImpl.getColumnWidth(availableSpace)

    override fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        _onItemClickListener = listener
    }

    override val dividerHeight2: Int
        get() = 1

    override var requestedHorizontalSpacing: Int
        get() = viewImpl.requestedHorizontalSpacing
        set(spacing) {
            viewImpl.requestedHorizontalSpacing = spacing
        }

    override fun fireOnItemClick(position: Int, v: View) {
        if (_onItemClickListener != null) {
            _onItemClickListener!!.onItemClick(this, v, position, v.id.toLong())
        }
    }

    override fun setOnItemLongClickListener(listener: AdapterView.OnItemLongClickListener) {
        _onItemLongClickListener = listener
    }

    override fun fireOnItemLongClick(position: Int, v: View): Boolean {
        return _onItemLongClickListener != null && _onItemLongClickListener!!
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
        viewImpl.requestedColumnWidth = width
    }

    fun setRequestedColumnCount(requestedColumnCount: Int) {
        viewImpl.requestedColumnCount = requestedColumnCount
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
}
