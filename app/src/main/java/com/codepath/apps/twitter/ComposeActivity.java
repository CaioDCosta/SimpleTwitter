package com.codepath.apps.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

	private TwitterClient client;
	public static final int MAX_TWEET_LENGTH = 280;
	EditText etComposeTweet;
	TextView tvCharCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		client = TwitterApp.getRestClient(this);
		etComposeTweet = findViewById(R.id.etComposeTweet);
		tvCharCount = findViewById(R.id.tvCharacterCount);
		etComposeTweet.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				return;
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int toDisplay = MAX_TWEET_LENGTH - s.length();
				tvCharCount.setText(String.format("%d",toDisplay));
				if(toDisplay >= 0) {
					tvCharCount.setTextColor(getResources().getColor(R.color.medium_green));
				}
				else {
					tvCharCount.setTextColor(getResources().getColor(R.color.medium_red));
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				return;
			}
		});
	}



	public void onClick(View view) {
		client.sendTweet(etComposeTweet.getText().toString(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				try {
					Tweet tweet = Tweet.fromJSON(response);
					Intent data = new Intent();
					data.putExtra("tweet", Parcels.wrap(tweet));
					setResult(RESULT_OK, data);
					finish();
				}
				catch(JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				throwable.printStackTrace();
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}
		});
	}
}
