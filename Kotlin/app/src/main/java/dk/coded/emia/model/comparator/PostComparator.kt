package dk.coded.emia.model.comparator

import java.util.Comparator

import dk.coded.emia.model.adapter.PostsCollectionViewItem

/**
 * Created by oldman on 12/10/17.
 */

class PostComparator : Comparator<PostsCollectionViewItem> {

    override fun compare(left: PostsCollectionViewItem, right: PostsCollectionViewItem): Int {
        val created1 = left.post!!.created
        val created2 = right.post!!.created
        return if (created1 < created2) {
            1
        } else {
            -1
        }
    }

}
