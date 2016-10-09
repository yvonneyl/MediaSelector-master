package yvonne.mediaselector_lib.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;

import yvonne.mediaselector_lib.BucketEntry;
import yvonne.mediaselector_lib.GalleryRetainCache;
import yvonne.mediaselector_lib.MediaChooserConstants;
import yvonne.mediaselector_lib.R;
import yvonne.mediaselector_lib.adapter.BucketGridAdapter;

import static android.os.Environment.getExternalStorageDirectory;


/**
 * Created by yvonne on 2016/9/19.
 */
public class MediaChooserBucketActivity extends MediaChooserBaseActivity implements View.OnClickListener {
    private static final int STARTED = 0;
    private static final int FINISHED = 1;
    private final int INDEX_BUCKET_ID = 0;
    private final int INDEX_BUCKET_NAME = 1;
    private final int INDEX_BUCKET_URL = 2;

    private static final String[] PROJECTION_BUCKET = {
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATA,
    };

    private BucketGridAdapter mBucketAdapter;
    private RecyclerView mGridView;
    private Cursor mCursor;
    private GridLayoutManager mLayoutManager;
    private Handler scanHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case STARTED:
                    showProgress("", "正在扫描本地视频..");
                    break;
                case FINISHED:
                    hideProgress();
                    init();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket);
        mGridView = (RecyclerView) findViewById(R.id.gridViewFromMediaChooser);
        mLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mGridView.setLayoutManager(mLayoutManager);
        SpacesItemDecoration decoration = new SpacesItemDecoration(8);
        mGridView.addItemDecoration(decoration);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        initActionBar();
        scanSdCard();
    }

    private void scanSdCard() {
        scanHandler.sendEmptyMessage(STARTED);
        MediaScannerConnection.scanFile(this, new String[]{getExternalStorageDirectory().getAbsolutePath()},
                null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        scanHandler.sendEmptyMessage(FINISHED);
                    }
                });
    }

    private void initActionBar() {
        /*AppToolbarManager.getInstance().initToolBar(this, R.layout.actionbar_setting);
        View actionBarView = AppToolbarManager.getInstance().getCustomView();
        actionBarView.findViewById(R.id.img_Back).setOnClickListener(this);
        ((MaterialMenuView) actionBarView.findViewById(R.id.img_Back)).setState(MaterialMenuDrawable.IconState.ARROW);
        ((TextView) actionBarView.findViewById(R.id.title)).setText("文件夹");*/

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
        ((TextView) actionBarView.findViewById(R.id.title)).setText("文件夹");
    }


    private void init() {
        final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        mCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_BUCKET, null, null, orderBy + " DESC");
        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();
        try {
            while (mCursor.moveToNext()) {
                BucketEntry entry = new BucketEntry(
                        mCursor.getInt(INDEX_BUCKET_ID),
                        mCursor.getString(INDEX_BUCKET_NAME), mCursor.getString(INDEX_BUCKET_URL));

                if (!buffer.contains(entry)) {
                    buffer.add(entry);
                }
            }
            if (mCursor.getCount() > 0) {
                mBucketAdapter = new BucketGridAdapter(this, buffer);
                mGridView.setAdapter(mBucketAdapter);
                mGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                            mBucketAdapter.setScrolling(true);
                        } else {
                            mBucketAdapter.setScrolling(false);
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }
                });
            } else {
                //ToastUtil.showMsg("no_media_file_available");
            }
            mBucketAdapter.setOnItemClickListener(new BucketGridAdapter.OnItemClickListener() {
                @Override
                public void onItemClickListener(int position) {
                    BucketEntry bucketEntry = mBucketAdapter.getItem(position);
                    Intent selectImageIntent = new Intent(MediaChooserBucketActivity.this, MediaChooserVideoActivity.class);
                    selectImageIntent.putExtra("name", bucketEntry.bucketName);
                    selectImageIntent.putExtra("isFromBucket", true);
                    startActivityForResult(selectImageIntent, MediaChooserConstants.BUCKET_SELECT_VIDEO_CODE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCursor.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaChooserConstants.BUCKET_SELECT_VIDEO_CODE && resultCode == RESULT_OK) {
            if (data.getStringExtra("path") != null) {
                data.setData(Uri.parse(data.getStringExtra("path")));
                setResult(RESULT_OK, data);
                //AppToolbarManager.getInstance().remove(this);
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }


    @Override
    public void refreshCurrentItems() {
        int firstPos = mLayoutManager.findFirstVisibleItemPosition();
        int lastPos = mLayoutManager.findLastVisibleItemPosition();
        if (mBucketAdapter.isScrolling()) {
            return;
        }
        mBucketAdapter.notifyItemRangeChanged(firstPos, lastPos - firstPos + 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
        if (GalleryRetainCache.getOrCreateRetainableCache().mRetainedCache != null) {
            GalleryRetainCache.getOrCreateRetainableCache().mRetainedCache.clear();
        }
    }
}
