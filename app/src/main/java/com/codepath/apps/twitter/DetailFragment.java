package com.codepath.apps.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends DialogFragment {
	public DetailFragment() {}

	@BindView(R.id.ivProfileImage) public ImageView ivProfileImage;
	@BindView(R.id.ivMedia) public ImageView ivMedia;
	@BindView(R.id.tvUserScreenName) public TextView tvUserScreenName;
	@BindView(R.id.tvBody) public TextView tvBody;
	@BindView(R.id.tvTimeStamp) public TextView tvTimeStamp;
	@BindView(R.id.tvUserName) public TextView tvUserName;
	@BindView(R.id.tvFavorite) public TextView tvFavorite;
	@BindView(R.id.tvRetweet) public TextView tvRetweet;
	@BindView(R.id.ibReply) public ImageButton ibReply;
	@BindView(R.id.ibFavorite) public ImageButton ibFavorite;
	@BindView(R.id.ibRetweet) public ImageButton ibRetweet;
	TwitterClient client;
	Tweet tweet;

	public static DetailFragment newInstance() {
		return new DetailFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
		return inflater.inflate(R.layout.fragment_detail, container);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);

		final JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler(){
		};

		ibReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(view.getContext(), ComposeActivity.class);
				intent.putExtra("tweet", Parcels.wrap(tweet));
				view.getContext().startActivity(intent);
			}
		});

		ibFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(!tweet.favorited) {
					tweet.favorited = true;
					tweet.numFavorite++;
					tvFavorite.setText(Integer.toString(tweet.numFavorite));
					client.likeTweet(tweet.uid, jsonHttpResponseHandler);
				}
				else {
					tweet.favorited = false;
					tweet.numFavorite--;
					tvFavorite.setText(Integer.toString(tweet.numFavorite));
					client.unlikeTweet(tweet.uid, jsonHttpResponseHandler);
				}
			}
		});
		ibRetweet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(!tweet.retweeted) {
					tweet.retweeted = true;
					client.reTweet(tweet.uid, jsonHttpResponseHandler);
					tweet.numRetweet++;
					tvRetweet.setText(Integer.toString(tweet.numRetweet));
				}
				else {
					tweet.retweeted = false;
					tweet.numRetweet--;
					client.unreTweet(tweet.uid, jsonHttpResponseHandler);
					tvRetweet.setText(Integer.toString(tweet.numRetweet));
				}

			}
		});
	}

}
