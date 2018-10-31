package dk.coded.library

import android.os.Parcelable

interface AsymmetricItem : Parcelable {
    val columnSpan: Int
    val rowSpan: Int
}
