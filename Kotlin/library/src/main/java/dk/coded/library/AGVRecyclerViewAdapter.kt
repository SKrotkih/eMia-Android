package dk.coded.library

import android.support.v7.widget.RecyclerView

abstract class AGVRecyclerViewAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    abstract fun getItem(position: Int): AsymmetricItem
}
