package com.codepath.apps.twitter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ComposeFragment extends DialogFragment {

	private TwitterClient client;
	public static final int MAX_TWEET_LENGTH = 280;
	@BindView(R.id.etComposeTweet) EditText etComposeTweet;
	@BindView(R.id.tvCharacterCount) TextView tvCharCount;
	@BindView(R.id.btnPostTweet) Button btnPostTweet;
	private Tweet tweet;

	public static ComposeFragment newInstance() {
		return new ComposeFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_compose, container);
	}


	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this, view);
		client = TwitterApp.getRestClient(getContext());
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
		if(getArguments() != null) {
			tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
			etComposeTweet.setText("@" + tweet.user.screenName);
		}

		btnPostTweet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSendTweet();
			}
		});

	}

	public interface ComposeDialogListener {
		void onFinishComposeDialog(Tweet tweet);
	}

	@Override
	public void onResume() {
		super.onResume();
		ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		params.height = ViewGroup.LayoutParams.MATCH_PARENT;
		getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	}

	public void onSendTweet() {
		if(etComposeTweet.getText().length() <= 280) {
			if(tweet != null) {
				client.sendTweetInReply(tweet.uid, etComposeTweet.getText().toString(), new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						try {
							Tweet tweet = Tweet.fromJSON(response);
							ComposeDialogListener listener = (ComposeDialogListener) getActivity();
							listener.onFinishComposeDialog(tweet);
							dismiss();
						} catch (JSONException e) {
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
			else {
				client.sendTweet(etComposeTweet.getText().toString(), new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						try {
							Tweet tweet = Tweet.fromJSON(response);
							ComposeDialogListener listener = (ComposeDialogListener) getActivity();
							listener.onFinishComposeDialog(tweet);
							dismiss();
						} catch (JSONException e) {
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
		else {
			Toast.makeText(getContext(), "Message too long!", Toast.LENGTH_SHORT).show();
		}
	}
}
