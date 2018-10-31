package dk.coded.library

interface PoolObjectFactory<T> {
    fun createObject(): T
}
