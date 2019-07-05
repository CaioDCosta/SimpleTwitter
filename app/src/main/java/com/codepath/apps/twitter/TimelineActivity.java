package com.codepath.apps.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.twitter.models.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

	private TwitterClient client;
	private TweetAdapter tweetAdapter;
	private ArrayList<Tweet> tweets;
	@BindView(R.id.rvTweets) RecyclerView rvTweets;

	@BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
	@BindView(R.id.fabCompose) FloatingActionButton fab;
	@BindView(R.id.toolbar) Toolbar toolbar;

	private EndlessRecyclerViewScrollListener scrollListener;
	private long minSeenID = Long.MAX_VALUE;

	private final int REQUEST_CODE = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);

		ButterKnife.bind(this);

		client = TwitterApp.getRestClient(this);

		// Set up RecyclerView and Adapter
		tweets = new ArrayList<>();
		tweetAdapter = new TweetAdapter(tweets);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		rvTweets.setLayoutManager(linearLayoutManager);
		rvTweets.setAdapter(tweetAdapter);

		// Set up the toolbar

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onComposeAction();
			}
		});

		// Set up Swipe Refresh
		swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				minSeenID = Long.MAX_VALUE;
				populateTimeline();
			}
		});

		// Set up scroll listener
		scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
			@Override
			public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
				fetchTimeline();
			}
		};
		rvTweets.addOnScrollListener(scrollListener);
		scrollListener.resetState();

		populateTimeline();

		swipeContainer.setColorSchemeResources(R.color.twitter_blue,
				R.color.twitter_blue_30,
				R.color.twitter_blue_50);
	}

	public void onComposeAction() {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
			tweets.add(0, tweet);
			tweetAdapter.notifyItemInserted(0);
			rvTweets.scrollToPosition(0);
		}
	}

	private void fetchTimeline() {
		client.getScrolledTimeline(minSeenID, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				List<Tweet> list = new ArrayList<Tweet>();
				for (int i = 0; i < response.length(); i++) {
					try {
						Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
						if (tweet.uid < minSeenID) minSeenID = tweet.uid;
						list.add(tweet);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				tweetAdapter.addAll(list);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if(statusCode == 429) {
					Toast.makeText(TimelineActivity.this, "Twitter Rate Limit Exceeded", Toast.LENGTH_LONG).show();
				}
				Log.d("TwitterClient", errorResponse.toString());
				throwable.printStackTrace();
			}
		});
	}

	private void populateTimeline() {
		client.getHomeTimeline(new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				List<Tweet> list = new ArrayList<Tweet>();
				for(int i = 0; i < response.length(); i++) {
					try {
						Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
						if(tweet.uid < minSeenID) minSeenID = tweet.uid;
						list.add(tweet);
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				}
				tweetAdapter.clear();
				scrollListener.resetState();
				tweetAdapter.addAll(list);
				swipeContainer.setRefreshing(false);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if(statusCode == 429) {
					Toast.makeText(TimelineActivity.this, "Twitter Rate Limit Exceeded", Toast.LENGTH_SHORT).show();
				}
				Log.d("TwitterClient", errorResponse.toString());
				throwable.printStackTrace();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.d("TwitterClient",response.toString());
				super.onSuccess(statusCode, headers, response);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
				throwable.printStackTrace();
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				throwable.printStackTrace();
				super.onFailure(statusCode, headers, responseString, throwable);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				Log.d("TwitterClient", responseString);
				super.onSuccess(statusCode, headers, responseString);
			}
		});
	}
}
