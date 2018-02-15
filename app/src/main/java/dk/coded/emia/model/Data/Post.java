package dk.coded.emia.model.Data;

import android.graphics.Bitmap;

import java.io.Serializable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dk.coded.emia.utils.Constants;

// [START post_class]
@IgnoreExtraProperties
public class Post implements Serializable {

    public String id;
    public String uid;
    public String author;
    public String title;
    public String body;
    public Bitmap photoBitmap;
    public long created;
    public Boolean portrait;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String title, String body, Bitmap bitmap) {
        this.title = title;
        this.body = body;
        this.photoBitmap = bitmap;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        this.portrait = w > h;
        this.created = System.currentTimeMillis()/1000;
    }

    public Calendar getDate() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(created * 1000);
        return date;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.Fields.Post.id, id);
        result.put(Constants.Fields.Post.uid, uid);
        result.put(Constants.Fields.Post.author, author);
        result.put(Constants.Fields.Post.title, title);
        result.put(Constants.Fields.Post.body, body);
        result.put(Constants.Fields.Post.starCount, starCount);
        result.put(Constants.Fields.Post.stars, stars);
        result.put(Constants.Fields.Post.portrait, portrait);
        result.put(Constants.Fields.Post.created, created);
        return result;
    }
    // [END post_to_map]

}
// [END post_class]
