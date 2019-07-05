package com.codepath.apps.twitter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

	public List<Tweet> mTweets;
	Context context;
	static TwitterClient client = null;

	public TweetAdapter(List<Tweet> tweets) {
		mTweets = tweets;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		if(client == null) client = TwitterApp.getRestClient(context);
		context = viewGroup.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
		return new ViewHolder(context, tweetView);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		Tweet tweet = mTweets.get(i);
		viewHolder.tvUserScreenName.setText("@" + tweet.user.screenName);
		viewHolder.tvBody.setText(tweet.body);
		viewHolder.tvTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));
		viewHolder.tvUserName.setText(tweet.user.name);
		Glide.with(context).load(tweet.user.profileImageUrl).bitmapTransform(new CropCircleTransformation(context))
				.placeholder(R.drawable.placeholder).into(viewHolder.ivProfileImage);
		if(tweet.mediaUrl != null) {
			viewHolder.ivMedia.setVisibility(View.VISIBLE);
			Glide.with(context).load(tweet.mediaUrl).bitmapTransform(new RoundedCornersTransformation(context, 15, 10))
					.placeholder(R.drawable.ic_vector_photo).into(viewHolder.ivMedia);
		}
		else {
			viewHolder.ivMedia.setVisibility(View.GONE);
		}
		viewHolder.tvFavorite.setText(Integer.toString(tweet.numFavorite));
		viewHolder.tvRetweet.setText(Integer.toString(tweet.numRetweet));
		viewHolder.tweet = tweet;
	}

	@Override
	public int getItemCount() {
		return mTweets.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
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
		Tweet tweet;

		View.OnClickListener onClickProfile = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("user", Parcels.wrap(tweet.user));
				context.startActivity(intent);
			}
		};

		public ViewHolder(final Context context, View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);

			final JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler(){
			};

			ivProfileImage.setOnClickListener(onClickProfile);
			tvUserName.setOnClickListener(onClickProfile);
			tvUserScreenName.setOnClickListener(onClickProfile);

			ibReply.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, ComposeActivity.class);
					intent.putExtra("tweet", Parcels.wrap(tweet));
					context.startActivity(intent);
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
							client.reTweet(tweet.uid, jsonHttpResponseHandler);
							tweet.numRetweet++;
							ibRetweet.setSelected(true);
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
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getAdapterPosition();
					if (position != RecyclerView.NO_POSITION) {
						// Get tweet at the current position
						Tweet tweet = mTweets.get(position);
						Bundle bundle = new Bundle();
						bundle.putParcelable("tweet", Parcels.wrap(tweet));
						FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
						DetailFragment detailFragment = DetailFragment.newInstance();
						detailFragment.setArguments(bundle);
						detailFragment.show(fm, "fragment_detail");
					}
				}
			});
		}
	}



	// getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
	public static String getRelativeTimeAgo(String rawJsonDate) {
		String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
		sf.setLenient(true);

		String relativeDate = "";
		try {
			long dateMillis = sf.parse(rawJsonDate).getTime();
			relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return relativeDate.replace("ago", "");
	}

	// Clean all elements of the recycler
	public void clear() {
		mTweets.clear();
		notifyDataSetChanged();
	}

	// Add a list of items -- change to type used
	public void addAll(List<Tweet> list) {
		mTweets.addAll(list);
		notifyDataSetChanged();
	}
}
