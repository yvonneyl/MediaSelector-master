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
import java.util.List;

import yvonne.mediaselector_lib.MediaModel;
import yvonne.mediaselector_lib.R;
import yvonne.mediaselector_lib.VideoLoadUtil;
import yvonne.mediaselector_lib.activity.MediaChooserVideoActivity;

import static yvonne.mediaselector_lib.activity.MediaChooserBaseActivity.SPAN_COUNT;

/**
 * Created by yvonne on 2016/9/19.
 */
public class GridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private MediaChooserVideoActivity activity;
    private List<MediaModel> mGalleryModelList = new ArrayList<>();
    LayoutInflater viewInflater;
    private boolean isScrolling = false;
    private OnItemClickListener mListener;
    private int mWidth;
    private OnItemLongClickListener mLongClickListener;

    public interface OnItemClickListener {
        void onItemClickListener(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemClickListener(int position);

    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mLongClickListener = listener;
    }

    public MediaModel getItem(int position) {
        return mGalleryModelList.get(position);
    }

    public GridViewAdapter(MediaChooserVideoActivity mActivity, List<MediaModel> categories) {
        mGalleryModelList.addAll(categories);
        activity = mActivity;
        viewInflater = LayoutInflater.from(mActivity);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = viewInflater.inflate(R.layout.view_grid_item_media_chooser, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        mWidth = activity.getResources().getDisplayMetrics().widthPixels;
        viewHolder.textView = (TextView) view.findViewById(R.id.nameTextViewFromMediaChooserGridItemRowView);
        viewHolder.imageView = (ImageView) view.findViewById(R.id.imageViewFromMediaChooserGridItemRowView);
        viewHolder.durationTv = (TextView) view.findViewById(R.id.duration_tv);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClickListener(position);
            }
        });
        myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mLongClickListener.onItemClickListener(position);
                return false;
            }
        });
        FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) myViewHolder.imageView.getLayoutParams();
        imageParams.width = mWidth / SPAN_COUNT;
        imageParams.height = mWidth / SPAN_COUNT;

        myViewHolder.imageView.setLayoutParams(imageParams);

        myViewHolder.imageView.setImageResource(R.drawable.ic_loading);
        myViewHolder.imageView.setTag(mGalleryModelList.get(position).url);
        new VideoLoadUtil().loadVideo(activity, myViewHolder.imageView, isScrolling, mWidth / SPAN_COUNT);
        myViewHolder.durationTv.setText(updateTextViewWithTimeFormatSecond((int) (mGalleryModelList.get(position).duration / 1000)));
        myViewHolder.textView.setText(mGalleryModelList.get(position).name);
    }

    public static String updateTextViewWithTimeFormatSecond(int second) {
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;

        String strTemp = null;

        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }
        return strTemp;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mGalleryModelList.size();
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView durationTv;

        public MyViewHolder(View view) {
            super(view);
        }
    }

}
