package dk.coded.emia.model.adapter

import android.os.Parcel
import android.os.Parcelable

import dk.coded.library.AsymmetricItem
import dk.coded.emia.model.Data.Post

class PostsCollectionViewItem : AsymmetricItem {
    var _columnSpan: Int = 0
    var _rowSpan: Int = 0
    var position: Int = 0
        private set
    var post: Post? = null

    @JvmOverloads
    constructor(columnSpan: Int = 1, rowSpan: Int = 1, position: Int = 0) {
        this._columnSpan = columnSpan
        this._rowSpan = rowSpan
        this.position = position
    }

    constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override var columnSpan: Int
        get() = _columnSpan
        set(value) {_columnSpan = value}

    override var rowSpan: Int
        get() = _rowSpan
        set(value) { _rowSpan = value}

    override fun toString(): String {
        return String.format("%s: %sx%s", position, rowSpan, columnSpan)
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun readFromParcel(`in`: Parcel) {
        columnSpan = `in`.readInt()
        rowSpan = `in`.readInt()
        position = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(columnSpan)
        dest.writeInt(rowSpan)
        dest.writeInt(position)
    }

    companion object {

        /* Parcelable interface implementation */
        @JvmField
        val CREATOR: Parcelable.Creator<PostsCollectionViewItem> = object : Parcelable.Creator<PostsCollectionViewItem> {
            override fun createFromParcel(`in`: Parcel): PostsCollectionViewItem {
                return PostsCollectionViewItem(`in`)
            }

            override fun newArray(size: Int): Array<PostsCollectionViewItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
