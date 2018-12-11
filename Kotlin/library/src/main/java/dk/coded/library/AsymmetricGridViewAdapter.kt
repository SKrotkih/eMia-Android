package dk.coded.library

import android.content.Context
import android.database.DataSetObserver
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.WrapperListAdapter

class AsymmetricGridViewAdapter(context: Context, listView: AsymmetricGridView, private val wrappedAdapter: ListAdapter) :
        BaseAdapter(), dk.coded.library.BaseAdapter<RecyclerView.ViewHolder>, WrapperListAdapter {
    private val adapterImpl: AdapterImpl

    override val actualItemCount: Int
        get() = wrappedAdapter.count

    internal inner class GridDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            recalculateItemsPerRow()
        }

        override fun onInvalidated() {
            recalculateItemsPerRow()
        }
    }

    init {
        this.adapterImpl = AdapterImpl(context, this, listView)
        wrappedAdapter.registerDataSetObserver(GridDataSetObserver())
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder = adapterImpl.onCreateViewHolder()
        adapterImpl.onBindViewHolder(viewHolder, position, parent)
        return viewHolder.itemView
    }

    override fun getItem(position: Int): AsymmetricGridItem {
        return wrappedAdapter.getItem(position) as AsymmetricGridItem
    }

    override fun onBindAsymmetricViewHolder(holder: AsymmetricGridViewHolder<RecyclerView.ViewHolder>, parent: ViewGroup, position: Int) {
        wrappedAdapter.getView(position, holder.itemView, parent)
    }

    override fun onCreateAsymmetricViewHolder(
            position: Int, parent: ViewGroup, viewType: Int): AsymmetricGridViewHolder<RecyclerView.ViewHolder> {
        return AsymmetricGridViewHolder<RecyclerView.ViewHolder>(wrappedAdapter.getView(position, null, parent))
    }

    override fun getItemId(position: Int): Long {
        return wrappedAdapter.getItemId(position)
    }

    override fun getCount(): Int {
        // Returns the row count for ListView display purposes
        return adapterImpl.rowCount
    }

    override fun getItemViewType(position: Int): Int {
        return wrappedAdapter.getItemViewType(position)
    }

    override fun getWrappedAdapter(): ListAdapter {
        return wrappedAdapter
    }

    internal fun recalculateItemsPerRow() {
        adapterImpl.recalculateItemsPerRow()
    }
}