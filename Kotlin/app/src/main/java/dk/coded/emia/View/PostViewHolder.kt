package dk.coded.emia.View

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import dk.coded.emia.R
import dk.coded.emia.model.Data.Post

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var titleView: TextView? = null
    var authorView: TextView
    var starView: ImageView
    var numStarsView: TextView
    var bodyView: TextView

    init {

        authorView = itemView.findViewById(R.id.post_author)
        starView = itemView.findViewById(R.id.star)
        numStarsView = itemView.findViewById(R.id.post_num_stars)
        bodyView = itemView.findViewById(R.id.post_body)
    }

    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        authorView.text = post.author
        numStarsView.text = post.starCount.toString()
        bodyView.text = post.body

        starView.setOnClickListener(starClickListener)
    }
}
