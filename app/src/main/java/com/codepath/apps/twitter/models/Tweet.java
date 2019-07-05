package com.codepath.apps.twitter.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {
	public String body;
	public long uid;
	public String createdAt;
	public User user;
	public int numFavorite;
	public int numRetweet;
	public int numReply;
	public boolean favorited;
	public boolean retweeted;

	public Tweet() {
	}

	// Deserialize the JSON
	public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
		Tweet tweet = new Tweet();
		tweet.body = jsonObject.getString("text");
		tweet.uid = jsonObject.getLong("id");
		tweet.createdAt = jsonObject.getString("created_at");
		tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
		tweet.numRetweet = jsonObject.getInt("retweet_count");
		tweet.numFavorite = jsonObject.getInt("favorite_count");
		tweet.favorited = jsonObject.getBoolean("favorited");
		tweet.retweeted = jsonObject.getBoolean("retweeted");
		return tweet;
	}
}
