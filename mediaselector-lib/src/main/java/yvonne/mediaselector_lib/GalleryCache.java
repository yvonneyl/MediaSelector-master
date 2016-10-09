package yvonne.mediaselector_lib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import yvonne.mediaselector_lib.activity.MediaChooserBaseActivity;


/**
 * Created by yvonne on 2016/9/19.
 */
public class GalleryCache {
    private LruCache<String, Bitmap> mBitmapCache;
    private HashMap<String, BitmapLoaderTask> taskMap;
    private ArrayList<BitmapLoaderTask> mTasks;
    private ArrayList<MediaMetadataRetriever> mMediaMetadataRetrievers;
    private String Tag = "GalleryCache";
    private int mMaxWidth;
    private Activity mActivity;


    public GalleryCache(int size, int maxWidth, int maxHeight) {
        mMaxWidth = maxWidth;

        mBitmapCache = new LruCache<String, Bitmap>(size) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        taskMap = new HashMap<String, BitmapLoaderTask>();
        mTasks = new ArrayList<BitmapLoaderTask>();
        mMediaMetadataRetrievers = new ArrayList<MediaMetadataRetriever>();
    }

    private void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            mBitmapCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromCache(String key) {
        return mBitmapCache.get(key);
    }


    public void loadBitmap(Activity mActivity, ImageView imageView, boolean isScrolling) {
        this.mActivity = mActivity;
        final Bitmap bitmap = getBitmapFromCache((String) imageView.getTag());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            /**
             * 做了限制MediaMetadataRetriever创建太多没有及时release会造成ANR
             * */
            if (mMediaMetadataRetrievers.size() > 1) {
                return;
            }

            if (mTasks.contains(taskMap.get(imageView.getTag().toString()))) {
                return;
            }

            BitmapLoaderTask task = new BitmapLoaderTask(imageView);
            task.bitmapLoader();
        }
    }

    /**
     * asyncTask 要慎用，cancel不掉，存在内存泄漏风险
     * rxJava 多线程加载图片，效率更高
     */
    private class BitmapLoaderTask {
        private ImageView mImageView;
        private Subscription subscription;

        private BitmapLoaderTask(ImageView imageView) {
            mImageView = imageView;
        }

        private void bitmapLoader() {
            final String mImageKey = mImageView.getTag().toString();
            taskMap.put(mImageKey, this);
            mTasks.add(this);
            subscription = Observable.just(mImageView)
                    .map(new Func1<ImageView, Bitmap>() {
                        @Override
                        public Bitmap call(ImageView mImageView) {
                            Bitmap bitmap = createVideoThumbnail(mImageKey, Thumbnails.MINI_KIND);
                            if (bitmap != null) {
                                addBitmapToCache(mImageKey, bitmap);
                                return bitmap;
                            }
                            return null;
                        }
                    }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Bitmap>() {
                        @Override
                        public void call(Bitmap bitmap) {
                            if (bitmap != null) {
                                mImageView.setImageBitmap(bitmap);
                                mTasks.remove(BitmapLoaderTask.this);
                            } else {
                                mImageView.setImageResource(R.drawable.ic_loading);
                            }
                            if (mActivity instanceof MediaChooserBaseActivity) {
                                ((MediaChooserBaseActivity) mActivity).refreshCurrentItems();
                            }
                        }
                    });
        }

        private Subscription getSubscription() {
            return subscription;
        }
    }

    private Bitmap createVideoThumbnail(String mImageKey, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        mMediaMetadataRetrievers.add(retriever);
        try {
            retriever.setDataSource(mImageKey);
            bitmap = retriever.getFrameAtTime();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
                mMediaMetadataRetrievers.remove(retriever);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }

        if (bitmap == null) {
            return null;
        }

        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 256) {
                float scale = 256f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, mMaxWidth, mMaxWidth,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public void clear() {
        if (mBitmapCache != null) {
            mBitmapCache.evictAll();
            clearTasks();
        }
    }

    private void clearTasks() {
        for (int i = 0; i < mTasks.size(); i++) {
            if (!mTasks.get(i).getSubscription().isUnsubscribed()) {
                mTasks.get(i).getSubscription().unsubscribe();
            }
        }
        mTasks.clear();

        for (int i = 0; i < mMediaMetadataRetrievers.size(); i++) {
            if (mMediaMetadataRetrievers.get(i) != null) {
                mMediaMetadataRetrievers.get(i).release();
            }
        }
        mMediaMetadataRetrievers.clear();
    }
}
