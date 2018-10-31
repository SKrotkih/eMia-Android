package dk.coded.emia.model.adapter

import android.os.Parcel
import android.os.Parcelable

import dk.coded.emia.AsymmetricItem
import dk.coded.emia.model.Data.Post

class PostsCollectionViewItem : AsymmetricItem {
    private var columnSpan: Int = 0
    private var rowSpan: Int = 0
    var position: Int = 0
        private set
    var post: Post? = null

    @JvmOverloads
    constructor(columnSpan: Int = 1, rowSpan: Int = 1, position: Int = 0) {
        this.columnSpan = columnSpan
        this.rowSpan = rowSpan
        this.position = position
    }

    constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun getColumnSpan(): Int {
        return columnSpan
    }

    override fun getRowSpan(): Int {
        return rowSpan
    }

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
        val CREATOR: Parcelable.Creator<PostsCollectionViewItem> = object : Parcelable.Creator<PostsCollectionViewItem> {
            override fun createFromParcel(`in`: Parcel): PostsCollectionViewItem {
                return PostsCollectionViewItem(`in`)
            }

            override fun newArray(size: Int): Array<PostsCollectionViewItem> {
                return arrayOfNulls(size)
            }
        }
    }
}
