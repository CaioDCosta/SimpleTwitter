package com.codepath.apps.twitter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

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
		Glide.with(context).load(tweet.user.profileImageUrl).bitmapTransform(new CropCircleTransformation(context)).placeholder(R.drawable.placeholder).into(viewHolder.ivProfileImage);
		viewHolder.tvFavorite.setText(Integer.toString(tweet.numFavorite));
		viewHolder.tvRetweet.setText(Integer.toString(tweet.numRetweet));
		viewHolder.favorited = tweet.favorited;
		viewHolder.retweeted = tweet.retweeted;
		viewHolder.tvUserName.setTag(tweet.uid);
	}

	@Override
	public int getItemCount() {
		return mTweets.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ImageView ivProfileImage;
		public TextView tvUserScreenName;
		public TextView tvBody;
		public TextView tvTimeStamp;
		public TextView tvUserName;
		public TextView tvFavorite;
		public TextView tvRetweet;
		public ImageButton ibReply;
		public ImageButton ibFavorite;
		public ImageButton ibRetweet;
		public boolean favorited;
		public boolean retweeted;

		private final int STATE_NORMAL = 0;
		private final int STATE_SELECTED = 1;

		public ViewHolder(final Context context, View itemView) {
			super(itemView);
			ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
			tvUserScreenName = itemView.findViewById(R.id.tvUserScreenName);
			tvUserName = itemView.findViewById(R.id.tvUserName);
			tvBody = itemView.findViewById(R.id.tvBody);
			tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
			tvFavorite = itemView.findViewById(R.id.tvFavorite);
			tvRetweet = itemView.findViewById(R.id.tvRetweet);
			ibReply = itemView.findViewById(R.id.ibReply);
			ibFavorite = itemView.findViewById(R.id.ibFavorite);
			ibRetweet = itemView.findViewById(R.id.ibRetweet);

			ibFavorite.setTag(favorited ? STATE_SELECTED : STATE_NORMAL);
			ibRetweet.setTag(retweeted ? STATE_SELECTED : STATE_NORMAL);

			final JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler(){
			};

			ibReply.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, ComposeActivity.class);
					intent.putExtra("username", tvUserScreenName.getText());
					context.startActivity(intent);
				}
			});

			ibFavorite.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					switch((int) v.getTag()) {
						case STATE_NORMAL:
							v.setSelected(true);
							v.setTag(STATE_SELECTED);
							tvFavorite.setText(Integer.toString(Integer.parseInt((String) tvFavorite.getText()) + 1));
							client.likeTweet((long) tvUserName.getTag(), jsonHttpResponseHandler);
							break;
						case STATE_SELECTED:
							v.setSelected(false);
							v.setTag(STATE_NORMAL);
							tvFavorite.setText(Integer.toString(Integer.parseInt((String) tvFavorite.getText()) - 1));
							client.unlikeTweet((long) tvUserName.getTag(), jsonHttpResponseHandler);
							break;
						default:
							// Shouldn't happen
							Log.d("TwitterClient", "Shouldn't have happened!");
					}

				}
			});
			ibRetweet.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					switch((int) v.getTag()) {
						case STATE_NORMAL:
							v.setSelected(true);
							v.setTag(STATE_SELECTED);
							client.reTweet((long) tvUserName.getTag(), jsonHttpResponseHandler);
							tvRetweet.setText(Integer.toString(Integer.parseInt((String) tvRetweet.getText()) + 1));
							break;
						case STATE_SELECTED:
							v.setSelected(false);
							v.setTag(STATE_NORMAL);
							client.unreTweet((long) tvUserName.getTag(), jsonHttpResponseHandler);
							tvRetweet.setText(Integer.toString(Integer.parseInt((String) tvRetweet.getText()) - 1));
							break;
						default:
							// Shouldn't happen
							Log.d("TwitterClient", "Shouldn't have happened!");
					}

				}
			});
		}
	}



	// getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
	public String getRelativeTimeAgo(String rawJsonDate) {
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
