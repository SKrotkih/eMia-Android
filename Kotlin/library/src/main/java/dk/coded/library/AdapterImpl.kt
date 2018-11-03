package dk.coded.library

import android.content.Context
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Color
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout

import java.util.ArrayList
import java.util.HashMap

class AdapterImpl(private val context: Context, val agvAdapter: AGVBaseAdapter<*>, val listView: AsymmetricView) : View.OnClickListener, View.OnLongClickListener {
    private val itemsPerRow = HashMap<Int, RowInfo>()
    private val linearLayoutPool: ObjectPool<LinearLayout>
    private val viewHoldersMap = ArrayMap<Int, ObjectPool<AsymmetricViewHolder<*>>>()
    private val debugEnabled: Boolean
    private var asyncTask: ProcessRowsTask? = null

    val rowCount: Int
        get() = itemsPerRow.size

    init {
        this.debugEnabled = listView.isDebugging
        this.linearLayoutPool = ObjectPool(LinearLayoutPoolObjectFactory(context))
    }

    private fun calculateItemsForRow(items: List<RowItem>, initialSpaceLeft: Float = listView.numColumns.toFloat()): RowInfo {
        val itemsThatFit = ArrayList<RowItem>()
        var currentItem = 0
        var rowHeight = 1
        var areaLeft = initialSpaceLeft

        while (areaLeft > 0 && currentItem < items.size) {
            val item = items[currentItem++]
            val itemArea = (item.item.rowSpan * item.item.columnSpan).toFloat()

            if (debugEnabled) {
                Log.d(TAG, String.format("item %s in row with height %s consumes %s area", item,
                        rowHeight, itemArea))
            }

            if (rowHeight < item.item.rowSpan) {
                // restart with double height
                itemsThatFit.clear()
                rowHeight = item.item.rowSpan
                currentItem = 0
                areaLeft = initialSpaceLeft * item.item.rowSpan
            } else if (areaLeft >= itemArea) {
                areaLeft -= itemArea
                itemsThatFit.add(item)
            } else if (!listView.isAllowReordering) {
                break
            }
        }

        return RowInfo(rowHeight, itemsThatFit, areaLeft)
    }

    fun recalculateItemsPerRow() {
        if (asyncTask != null) {
            asyncTask!!.cancel(true)
        }

        linearLayoutPool.clear()
        itemsPerRow.clear()

        asyncTask = ProcessRowsTask()
        asyncTask!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }

    override fun onClick(v: View) {

        val rowItem = v.tag as ViewState
        listView.fireOnItemClick(rowItem.rowItem.index, v)
    }

    override fun onLongClick(v: View): Boolean {

        val rowItem = v.tag as ViewState
        return listView.fireOnItemLongClick(rowItem.rowItem.index, v)
    }

    fun onBindViewHolder(holder: ViewHolder, position: Int, parent: ViewGroup) {
        if (debugEnabled) {
            Log.d(TAG, "onBindViewHolder(" + position.toString() + ")")
        }

        val rowInfo = itemsPerRow[position] ?: return

        val rowItems = ArrayList(rowInfo.getItems())
        val layout = initializeLayout(holder.itemView())
        // Index to control the current position of the current column in this row
        var columnIndex = 0
        // Index to control the current position in the array of all the items available for this row
        var currentIndex = 0
        var spaceLeftInColumn = rowInfo.rowHeight

        while (!rowItems.isEmpty() && columnIndex < listView.numColumns) {
            val currentItem = rowItems[currentIndex]

            if (spaceLeftInColumn == 0) {
                // No more space in this column. Move to next one
                columnIndex++
                currentIndex = 0
                spaceLeftInColumn = rowInfo.rowHeight
                continue
            }

            // Is there enough space in this column to accommodate currentItem?
            if (spaceLeftInColumn >= currentItem.item.rowSpan) {
                rowItems.remove(currentItem)

                val actualIndex = currentItem.index
                val viewType = agvAdapter.getItemViewType(actualIndex)
                var pool: ObjectPool<AsymmetricViewHolder<*>>? = viewHoldersMap[viewType]
                if (pool == null) {
                    pool = ObjectPool()
                    viewHoldersMap[viewType] = pool
                }
                var viewHolder = pool.get()
                if (viewHolder == null) {
                    viewHolder = agvAdapter.onCreateAsymmetricViewHolder(actualIndex, parent, viewType)
                }
                //agvAdapter.onBindAsymmetricViewHolder(viewHolder, parent, actualIndex)
                val view = viewHolder!!.itemView
                view.tag = ViewState(viewType, currentItem, viewHolder)
                view.setOnClickListener(this)
                view.setOnLongClickListener(this)

                spaceLeftInColumn -= currentItem.item.rowSpan
                currentIndex = 0

                view.layoutParams = LinearLayout.LayoutParams(getRowWidth(currentItem.item),
                        getRowHeight(currentItem.item))

                val childLayout = findOrInitializeChildLayout(layout, columnIndex)
                childLayout.addView(view)
            } else if (currentIndex < rowItems.size - 1) {
                // Try again with next item
                currentIndex++
            } else {
                break
            }
        }

        if (debugEnabled && position % 20 == 0) {
            Log.d(TAG, linearLayoutPool.getStats("LinearLayout"))
            for ((key, value) in viewHoldersMap) {
                Log.d(TAG, value.getStats("ConvertViewMap, viewType=$key"))
            }
        }
    }

    fun onCreateViewHolder(): ViewHolder {
        if (debugEnabled) {
            Log.d(TAG, "onCreateViewHolder()")
        }

        val layout = LinearLayout(context, null)
        if (debugEnabled) {
            layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        layout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        layout.dividerDrawable = ContextCompat.getDrawable(context, R.drawable.item_divider_horizontal)

        val layoutParams = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
        layout.layoutParams = layoutParams
        return ViewHolder(layout)
    }

    fun getRowHeight(item: AsymmetricItem): Int {
        return getRowHeight(item.rowSpan)
    }

    fun getRowHeight(rowSpan: Int): Int {
        val rowHeight = listView.columnWidth * rowSpan
        // when the item spans multiple rows, we need to account for the vertical padding
        // and add that to the total final height
        return rowHeight + (rowSpan - 1) * listView.separatorHeight
    }

    fun getRowWidth(item: AsymmetricItem): Int {
        return getRowWidth(item.columnSpan)
    }

    protected fun getRowWidth(columnSpan: Int): Int {
        val rowWidth = listView.columnWidth * columnSpan
        // when the item spans multiple columns, we need to account for the horizontal padding
        // and add that to the total final width
        return Math.min(rowWidth + (columnSpan - 1) * listView.requestedHorizontalSpacing,
                Utils.getScreenWidth(context))
    }

    private fun initializeLayout(layout: LinearLayout): LinearLayout {
        // Clear all layout children before starting
        val childCount = layout.childCount
        for (j in 0 until childCount) {
            val tempChild = layout.getChildAt(j) as LinearLayout
            linearLayoutPool.put(tempChild)
            val innerChildCount = tempChild.childCount
            for (k in 0 until innerChildCount) {
                val innerView = tempChild.getChildAt(k)
                val viewState = innerView.tag as ViewState
                val pool = viewHoldersMap[viewState.viewType]
                pool!!.put(viewState.viewHolder)
            }
            tempChild.removeAllViews()
        }
        layout.removeAllViews()

        return layout
    }

    private fun findOrInitializeChildLayout(parentLayout: LinearLayout, childIndex: Int): LinearLayout {
        var childLayout = parentLayout.getChildAt(childIndex) as? LinearLayout

        if (childLayout == null) {
            childLayout = linearLayoutPool.get()
            childLayout!!.orientation = LinearLayout.VERTICAL

            if (debugEnabled) {
                childLayout.setBackgroundColor(Color.parseColor("#837BF2"))
            }

            childLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            childLayout.dividerDrawable = ContextCompat.getDrawable(context, R.drawable.item_divider_vertical)

            childLayout.layoutParams = AbsListView.LayoutParams(
                    AbsListView.LayoutParams.WRAP_CONTENT,
                    AbsListView.LayoutParams.MATCH_PARENT)

            parentLayout.addView(childLayout)
        }

        return childLayout
    }

    internal inner class ProcessRowsTask : AsyncTask<Void, Void, List<RowInfo>>() {
        override fun doInBackground(vararg params: Void): List<RowInfo> {
            // We need a map in order to associate the item position in the wrapped adapter.
            val itemsToAdd = ArrayList<RowItem>()
            for (i in 0 until agvAdapter.actualItemCount) {
                try {
                    itemsToAdd.add(RowItem(i, agvAdapter.getItem(i)))
                } catch (e: CursorIndexOutOfBoundsException) {
                    Log.w(TAG, e)
                }

            }

            return calculateItemsPerRow(itemsToAdd)
        }

        override fun onPostExecute(rows: List<RowInfo>) {
            for (row in rows) {
                itemsPerRow[rowCount] = row
            }

            if (debugEnabled) {
                for ((key, value) in itemsPerRow) {
                    Log.d(TAG, "row: " + key + ", items: " + value.getItems().size)
                }
            }

            agvAdapter.notifyDataSetChanged()
        }

        private fun calculateItemsPerRow(itemsToAdd: MutableList<RowItem>): List<RowInfo> {
            val rows = ArrayList<RowInfo>()

            while (!itemsToAdd.isEmpty()) {
                val stuffThatFit = calculateItemsForRow(itemsToAdd)
                val itemsThatFit = stuffThatFit.getItems()

                if (itemsThatFit.isEmpty()) {
                    // we can't fit a single item inside a row.
                    // bail out.
                    break
                }

                for (entry in itemsThatFit) {
                    itemsToAdd.remove(entry)
                }

                rows.add(stuffThatFit)
            }

            return rows
        }
    }

    private class ViewState constructor(val viewType: Int, val rowItem: RowItem, val viewHolder: AsymmetricViewHolder<*>)

    class ViewHolder(itemView: LinearLayout) : RecyclerView.ViewHolder(itemView) {

        fun itemView(): LinearLayout {
            return itemView as LinearLayout
        }
    }

    companion object {
        private val TAG = "AdapterImpl"
    }
}
