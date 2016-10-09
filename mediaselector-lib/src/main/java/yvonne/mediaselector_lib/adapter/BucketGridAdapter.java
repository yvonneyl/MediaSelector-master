/*
 * Copyright 2013 - learnNcode (learnncode@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package yvonne.mediaselector_lib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import yvonne.mediaselector_lib.BucketEntry;
import yvonne.mediaselector_lib.R;
import yvonne.mediaselector_lib.VideoLoadUtil;
import yvonne.mediaselector_lib.activity.MediaChooserBucketActivity;

import static yvonne.mediaselector_lib.activity.MediaChooserBaseActivity.SPAN_COUNT;

/**
 * Created by yvonne on 2016/9/19.
 */
public class BucketGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private MediaChooserBucketActivity mContext;
    private ArrayList<BucketEntry> mBucketEntryList;
    private int mWidth;
    LayoutInflater viewInflater;
    private OnItemClickListener mListener;
    private boolean isScrolling = false;

    public interface OnItemClickListener {
        void onItemClickListener(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public BucketEntry getItem(int position) {
        return mBucketEntryList.get(position);
    }

    public BucketGridAdapter(MediaChooserBucketActivity context, ArrayList<BucketEntry> categories) {
        mBucketEntryList = categories;
        mContext = context;
        viewInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        View view = viewInflater.inflate(R.layout.view_grid_bucket_item_media_chooser, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        holder.imageView = (ImageView) view.findViewById(R.id.imageViewFromMediaChooserBucketRowView);
        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextViewFromMediaChooserBucketRowView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) viewHolder.imageView.getLayoutParams();
        imageParams.width = mWidth / SPAN_COUNT;
        imageParams.height = mWidth / SPAN_COUNT;
        viewHolder.imageView.setLayoutParams(imageParams);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClickListener(position);
            }
        });
        viewHolder.imageView.setTag(mBucketEntryList.get(position).bucketUrl);
        new VideoLoadUtil().loadVideo(mContext, viewHolder.imageView, isScrolling, mWidth / 3);
        viewHolder.nameTextView.setText(mBucketEntryList.get(position).bucketName);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mBucketEntryList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }
}


