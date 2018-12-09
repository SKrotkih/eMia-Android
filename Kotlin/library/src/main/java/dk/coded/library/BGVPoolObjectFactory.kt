package dk.coded.library

interface BGVPoolObjectFactory<T> {
    fun createObject(): T
}
