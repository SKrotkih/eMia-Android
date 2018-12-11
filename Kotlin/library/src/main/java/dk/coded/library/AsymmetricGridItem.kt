package dk.coded.library

import android.os.Parcelable

interface AsymmetricGridItem : Parcelable {
    val columnSpan: Int
    val rowSpan: Int
}
