package dk.coded.library

import android.os.Parcel
import android.os.Parcelable

import java.util.Stack

internal class BGVObjectPool<T> : Parcelable {
    var stack = Stack<T>()
    var factory: BasePoolObjectFactory<T>? = null
    var stats = PoolStats()

    constructor(`in`: Parcel) {}

    constructor() {
    }

    constructor(factory: BasePoolObjectFactory<T>) {
        this.factory = factory
    }

    internal class PoolStats {
        var size = 0
        var hits = 0
        var misses = 0
        var created = 0

        fun getStats(name: String): String {
            return String.format("%s: size %d, hits %d, misses %d, created %d", name, size, hits,
                    misses, created)
        }
    }

    fun get(): T? {
        if (!stack.isEmpty()) {
            stats.hits++
            stats.size--
            return stack.pop()
        }

        stats.misses++

        val `object` = if (factory != null) factory!!.createObject() else null

        if (`object` != null) {
            stats.created++
        }

        return `object`
    }

    fun put(`object`: T) {
        stack.push(`object`)
        stats.size++
    }

    fun clear() {
        stats = PoolStats()
        stack.clear()
    }

    fun getStats(name: String): String {
        return stats.getStats(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<BGVObjectPool<*>> = object : Parcelable.Creator<BGVObjectPool<*>> {

            override fun createFromParcel(`in`: Parcel): BGVObjectPool<*> {
                return BGVObjectPool<Parcel>(`in`)
            }

            override fun newArray(size: Int): Array<BGVObjectPool<*>?> {
                return arrayOfNulls(size)
            }
        }
    }
}
