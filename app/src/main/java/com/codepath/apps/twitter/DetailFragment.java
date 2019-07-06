package com.codepath.apps.twitter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

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
		return inflater.inflate(R.layout.fragment_detail, container);
	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
		client = TwitterApp.getRestClient(getContext());
		tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
		tvUserScreenName.setText("@" + tweet.user.screenName);
		tvBody.setText(tweet.body);
		tvTimeStamp.setText(TweetAdapter.getRelativeTimeAgo(tweet.createdAt));
		tvUserName.setText(tweet.user.name);
		Glide.with(getContext()).load(tweet.user.profileImageUrl).bitmapTransform(new CropCircleTransformation(getContext()))
				.placeholder(R.drawable.placeholder).into(ivProfileImage);
		if(tweet.mediaUrl != null) {
			ivMedia.setVisibility(View.VISIBLE);
			Glide.with(getContext()).load(tweet.mediaUrl).bitmapTransform(new RoundedCornersTransformation(getContext(), 15, 10))
					.placeholder(R.drawable.ic_vector_photo).into(ivMedia);
		}
		else {
			ivMedia.setVisibility(View.GONE);
		}
		tvFavorite.setText(Integer.toString(tweet.numFavorite));
		tvRetweet.setText(Integer.toString(tweet.numRetweet));

		final JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler(){
		};

		ibReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putParcelable("tweet", Parcels.wrap(tweet));
				FragmentManager fm = ((AppCompatActivity) getContext()).getSupportFragmentManager();
				ComposeFragment composeFragment = ComposeFragment.newInstance();
				composeFragment.setArguments(bundle);
				composeFragment.show(fm, "fragment_compose");
			}
		});

		ibFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(!tweet.favorited) {
					tweet.favorited = true;
					tweet.numFavorite++;
					ibFavorite.setSelected(true);
					tvFavorite.setText(Integer.toString(tweet.numFavorite));
					client.likeTweet(tweet.uid, jsonHttpResponseHandler);
				}
				else {
					tweet.favorited = false;
					tweet.numFavorite--;
					ibFavorite.setSelected(false);
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
					ibRetweet.setSelected(true);
					client.reTweet(tweet.uid, jsonHttpResponseHandler);
					tweet.numRetweet++;
					tvRetweet.setText(Integer.toString(tweet.numRetweet));
				}
				else {
					tweet.retweeted = false;
					tweet.numRetweet--;
					ibRetweet.setSelected(false);
					client.unreTweet(tweet.uid, jsonHttpResponseHandler);
					tvRetweet.setText(Integer.toString(tweet.numRetweet));
				}

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	}

}
