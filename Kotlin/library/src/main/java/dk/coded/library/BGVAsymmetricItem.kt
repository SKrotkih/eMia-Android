package dk.coded.library

import android.os.Parcelable

interface BGVAsymmetricItem : Parcelable {
    val columnSpan: Int
    val rowSpan: Int
}
