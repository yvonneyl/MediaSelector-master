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


package yvonne.mediaselector_lib;

import android.app.Activity;
import android.widget.ImageView;


/**
 * Created by yvonne on 2016/9/19.
 */
public class VideoLoadUtil {


    public void loadVideo(Activity mActivity, ImageView imageView, boolean isScrolling, int width) {

        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        GalleryRetainCache c = GalleryRetainCache.getOrCreateRetainableCache();
        GalleryCache mCache = c.mRetainedCache;

        if (mCache == null) {
            mCache = new GalleryCache(cacheSize, width, width);
            c.mRetainedCache = mCache;
        }
        mCache.loadBitmap(mActivity, imageView, isScrolling);
    }
}
