package com.codepath.apps.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

	private TwitterClient client;
	TweetAdapter tweetAdapter;
	ArrayList<Tweet> tweets;
	RecyclerView rvTweets;

	private final int REQUEST_CODE = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		
		client = TwitterApp.getRestClient(this);

		rvTweets = findViewById(R.id.rvTweets);

		tweets = new ArrayList<>();

		tweetAdapter = new TweetAdapter(tweets);

		rvTweets.setLayoutManager(new LinearLayoutManager(this));

		rvTweets.setAdapter(tweetAdapter);

		// Find the toolbar view inside the activity layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		// Sets the Toolbar to act as the ActionBar for this Activity window.
		// Make sure the toolbar exists in the activity and is not null
		setSupportActionBar(toolbar);

		populateTimeline();
	}

	public void onComposeAction() {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_timeline, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case(R.id.miCompose):
				onComposeAction();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
		tweets.add(0, tweet);
		tweetAdapter.notifyItemInserted(0);
		rvTweets.scrollToPosition(0);
	}

	private void populateTimeline() {
		client.getHomeTimeline(new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				for(int i = 0; i < response.length(); i++) {
					try {
						Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
						tweets.add(tweet);
						tweetAdapter.notifyItemInserted(tweets.size() - 1);
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.d("TwitterClient", response.toString());
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.d("TwitterClient", responseString);
				throwable.printStackTrace();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
				Log.d("TwitterClient", errorResponse.toString());
				throwable.printStackTrace();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				Log.d("TwitterClient", errorResponse.toString());
				throwable.printStackTrace();
			}
		});
	}
}
