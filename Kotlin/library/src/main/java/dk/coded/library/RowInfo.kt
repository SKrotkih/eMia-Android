package dk.coded.library

import android.os.Parcel
import android.os.Parcelable

import java.util.ArrayList

internal class RowInfo : Parcelable {
    val _items: MutableList<RowItem>
    val rowHeight: Int
    val spaceLeft: Float

    constructor(rowHeight: Int, items: MutableList<RowItem>, spaceLeft: Float) {
        this.rowHeight = rowHeight
        this._items = items
        this.spaceLeft = spaceLeft
    }

    constructor(`in`: Parcel) {
        rowHeight = `in`.readInt()
        spaceLeft = `in`.readFloat()
        val totalItems = `in`.readInt()

        _items = ArrayList()
        val classLoader = AsymmetricGridItem::class.java!!.getClassLoader()

        for (i in 0 until totalItems) {
            _items.add(RowItem(`in`.readInt(), `in`.readParcelable<Parcelable>(classLoader) as AsymmetricGridItem))
        }
    }

    fun getItems(): List<RowItem> {
        return _items
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(rowHeight)
        dest.writeFloat(spaceLeft)
        dest.writeInt(_items.size)

        for (rowItem in _items) {
            dest.writeInt(rowItem.index)
            dest.writeParcelable(rowItem.item, 0)
        }
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<RowInfo> = object : Parcelable.Creator<RowInfo> {
            override fun createFromParcel(`in`: Parcel): RowInfo {
                return RowInfo(`in`)
            }

            override fun newArray(size: Int): Array<RowInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}
