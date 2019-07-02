package com.codepath.apps.twitter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

	public List<Tweet> mTweets;
	Context context;

	public TweetAdapter(List<Tweet> tweets) {
		mTweets = tweets;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		context = viewGroup.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
		return new ViewHolder(tweetView);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
		Tweet tweet = mTweets.get(i);
		viewHolder.tvUsername.setText(tweet.user.name);
		viewHolder.tvBody.setText(tweet.body);
		viewHolder.tvTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));
		Glide.with(context).load(tweet.user.profileImageUrl).bitmapTransform(new CropCircleTransformation(context)).placeholder(R.drawable.placeholder).into(viewHolder.ivProfileImage);
	}

	@Override
	public int getItemCount() {
		return mTweets.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ImageView ivProfileImage;
		public TextView tvUsername;
		public TextView tvBody;
		public TextView tvTimeStamp;

		public ViewHolder(View itemView) {
			super(itemView);
			ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
			tvUsername = itemView.findViewById(R.id.tvUserName);
			tvBody = itemView.findViewById(R.id.tvBody);
			tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
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
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return relativeDate;
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
