package com.codepath.apps.twitter.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {
	public String name;
	public long uid;
	public String screenName;
	public String profileImageUrl;

	public User() {
	}

	// Deserialize the JSON
	public static User fromJSON(JSONObject json) throws JSONException {
		User user = new User();

		user.name = json.getString("name");
		user.uid = json.getLong("id");
		user.screenName = json.getString("screen_name");
		user.profileImageUrl = json.getString("profile_image_url_https");
		return user;
	}
}
