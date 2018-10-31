package dk.coded.emia.model.adapter

import android.widget.ListAdapter

import dk.coded.emia.model.adapter.PostsCollectionViewItem

interface PostsListAdapterProtocol : ListAdapter {

    fun startListening()

    fun stopListening()

    fun appendItems(newItems: List<PostsCollectionViewItem>)

    fun setItems(moreItems: List<PostsCollectionViewItem>)
}
