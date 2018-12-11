package dk.coded.library

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

interface BaseAdapter<T : RecyclerView.ViewHolder> {
    val actualItemCount: Int
    fun getItem(position: Int): AsymmetricGridItem
    fun notifyDataSetChanged()
    fun getItemViewType(actualIndex: Int): Int
    fun onCreateAsymmetricViewHolder(position: Int, parent: ViewGroup, viewType: Int): AsymmetricGridViewHolder<T>
    fun onBindAsymmetricViewHolder(holder: AsymmetricGridViewHolder<T>, parent: ViewGroup, position: Int)
}
