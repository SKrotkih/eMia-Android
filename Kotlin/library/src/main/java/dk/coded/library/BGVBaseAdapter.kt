package dk.coded.library

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

interface BGVBaseAdapter<T : RecyclerView.ViewHolder> {
    val actualItemCount: Int
    fun getItem(position: Int): BGVAsymmetricItem
    fun notifyDataSetChanged()
    fun getItemViewType(actualIndex: Int): Int
    fun onCreateAsymmetricViewHolder(position: Int, parent: ViewGroup, viewType: Int): BGVAsymmetricViewHolder<T>
    fun onBindAsymmetricViewHolder(holder: BGVAsymmetricViewHolder<T>, parent: ViewGroup, position: Int)
}
