package yvonne.mediaselector_lib.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import yvonne.mediaselector_lib.GalleryRetainCache;
import yvonne.mediaselector_lib.MediaChooser;
import yvonne.mediaselector_lib.MediaModel;
import yvonne.mediaselector_lib.R;
import yvonne.mediaselector_lib.adapter.GridViewAdapter;

/**
 * Created by yvonne on 2016/9/19.
 */
public class MediaChooserVideoActivity extends MediaChooserBaseActivity implements View.OnClickListener {
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;

    private GridViewAdapter mVideoAdapter;
    private RecyclerView mVideoGridView;
    private Cursor mCursor;
    private GridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_chooser_video);
        mVideoGridView = (RecyclerView) findViewById(R.id.gridViewFromMediaChooser);
        mLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mVideoGridView.setLayoutManager(mLayoutManager);
        SpacesItemDecoration decoration = new SpacesItemDecoration(8);
        mVideoGridView.addItemDecoration(decoration);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        if (getIntent() != null) {
            initVideos(getIntent().getStringExtra("name"));
        } else {
            initVideos();
        }
        initActionBar();
    }

    private void initActionBar() {
       /* AppToolbarManager.getInstance().initToolBar(this, R.layout.actionbar_setting);
        View actionBarView = AppToolbarManager.getInstance().getCustomView();
        actionBarView.findViewById(R.id.img_Back).setOnClickListener(this);
        ((MaterialMenuView) actionBarView.findViewById(R.id.img_Back)).setState(MaterialMenuDrawable.IconState.ARROW);
        ((TextView) actionBarView.findViewById(R.id.title)).setText("本地视频");*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        toolbar.setTitle("");// 标题的文字需在setSupportActionBar之前，不然会无效
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_media_chooser);
        View topView = findViewById(R.id.top_view);
        if (topView != null) {
            topView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        View actionBarView = getSupportActionBar().getCustomView();
        actionBarView.findViewById(R.id.img_Back).setOnClickListener(this);
        ((TextView) actionBarView.findViewById(R.id.title)).setText("本地视频");
    }

    private void initVideos(String bucketName) {

        try {
            final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
            String searchParams = "bucket_display_name = \"" + bucketName + "\"";
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.DURATION};
            mCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, searchParams, null, orderBy + " DESC");
            setAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initVideos() {

        try {
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;

            String[] proj = {MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID};

            mCursor = getContentResolver().query(MEDIA_EXTERNAL_CONTENT_URI, proj, null, null, orderBy + " DESC");
            setAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setAdapter() {
        int count = mCursor.getCount();

        if (count > 0) {

            mCursor.moveToFirst();

            ArrayList<MediaModel> mGalleryModelList = new ArrayList<MediaModel>();
            for (int i = 0; i < count; i++) {
                mCursor.moveToPosition(i);
                String url = mCursor.getString(mCursor.getColumnIndex(MEDIA_DATA));
                String name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                long duration = Long.parseLong(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                mGalleryModelList.add(new MediaModel(url, name, duration));
            }

            mVideoAdapter = new GridViewAdapter(this, mGalleryModelList);
            mVideoGridView.setAdapter(mVideoAdapter);
            mVideoGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        mVideoAdapter.setScrolling(true);
                    } else {
                        mVideoAdapter.setScrolling(false);
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        mVideoAdapter.setOnItemLongClickListener(new GridViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemClickListener(int position) {
                MediaModel galleryModel = mVideoAdapter.getItem(position);
                File file = new File(galleryModel.url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                startActivity(intent);
            }
        });


        mVideoAdapter.setOnItemClickListener(new GridViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                MediaModel galleryModel = mVideoAdapter.getItem(position);
                Intent videoIntent = new Intent();
                videoIntent.setAction(MediaChooser.VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
                videoIntent.putExtra("path", galleryModel.url);
                setResult(RESULT_OK, videoIntent);
                // AppToolbarManager.getInstance().remove(MediaChooserVideoActivity.this);
                finish();
            }
        });
    }


    @Override
    public void refreshCurrentItems() {
        int firstPos = mLayoutManager.findFirstVisibleItemPosition();
        int lastPos = mLayoutManager.findLastVisibleItemPosition();
        if (mVideoAdapter.isScrolling()) {
            return;
        }
        mVideoAdapter.notifyItemRangeChanged(firstPos, lastPos - firstPos + 1);
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
        GalleryRetainCache.getOrCreateRetainableCache().mRetainedCache.clear();
    }
}
