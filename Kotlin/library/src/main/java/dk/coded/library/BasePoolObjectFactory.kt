package dk.coded.library

interface BasePoolObjectFactory<T> {
    fun createObject(): T
}
