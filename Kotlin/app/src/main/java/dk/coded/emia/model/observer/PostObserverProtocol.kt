package dk.coded.emia.model.observer

import dk.coded.emia.model.Data.Post

/**
 * Created by oldman on 12/10/17.
 */

interface PostObserverProtocol {

    fun update(posts: List<Post>)
    fun newPost(post: Post)

}
