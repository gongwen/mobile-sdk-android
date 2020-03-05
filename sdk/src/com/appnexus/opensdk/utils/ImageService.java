/*
 *    Copyright 2014 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;

import com.appnexus.opensdk.SDKSettings;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ImageService {
    HashMap<String, String> imageUrlMap = new HashMap<>();
    ImageServiceListener imageServiceListener;

    static final int TIMEOUT = 10000;
    private ImageReceiver imageReceiver;

    public void registerImageReceiver(ImageReceiver imageReceiver, HashMap<String, String> imageUrlMap) {
        if (imageReceiver != null && imageUrlMap != null && !imageUrlMap.isEmpty()) {
            this.imageReceiver = imageReceiver;
            this.imageUrlMap = imageUrlMap;
        }
    }

    public void registerNotification(ImageServiceListener imageServiceListener) {
        this.imageServiceListener = imageServiceListener;
    }

    public void finishDownload(String key) {
        if (imageUrlMap != null) {
            if (imageUrlMap.containsKey(key)) {
                imageUrlMap.remove(key);
                if (imageUrlMap.size() == 0) {
                    imageServiceListener.onAllImageDownloadsFinish();
                    Clog.d(Clog.baseLogTag, "Images downloading finished.");
                    finish();
                }
            }
        }
    }

    private void finish() {
        imageUrlMap = null;
        imageServiceListener = null;
    }


    public void execute() {
        if (imageServiceListener == null) {
            finish();
            return;
        }
        if (imageUrlMap != null && !imageUrlMap.isEmpty()) {
            for (Map.Entry pairs : imageUrlMap.entrySet()) {
                ImageDownloader downloader = new ImageDownloader(imageReceiver, (String) pairs.getKey(), (String) pairs.getValue(), this);
                Clog.d(Clog.baseLogTag, "Downloading " + pairs.getKey() + " from url: " + pairs.getValue());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    downloader.executeOnExecutor(SDKSettings.getExternalExecutor());
                } else {
                    downloader.execute();
                }
            }
        } else {
            imageServiceListener.onAllImageDownloadsFinish();
            finish();
        }
    }

    class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
        private final String key;
        WeakReference<ImageService> caller;
        WeakReference<ImageReceiver> imageReceiver;
        String url;

        ImageDownloader(ImageReceiver imageReceiver, String key, String url, ImageService caller) {
            this.caller = new WeakReference<ImageService>(caller);
            this.imageReceiver = new WeakReference<ImageReceiver>(imageReceiver);
            this.url = url;
            this.key = key;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            imageReceiver.clear();
            caller.clear();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (isCancelled() || StringUtil.isEmpty(url)) {
                return null;
            }
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setReadTimeout(TIMEOUT);
                InputStream is = (InputStream) connection.getContent();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;

            } catch (Exception ignore) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            ImageReceiver receiver = imageReceiver.get();
            ImageService service = caller.get();
            if (receiver != null) {
                if (image == null) {
                    receiver.onFail(url);
                } else {
                    receiver.onReceiveImage(key, image);
                }
            }
            if (service != null) {
                service.finishDownload(key);
            }
        }
    }

    public interface ImageReceiver {
        void onReceiveImage(String key, Bitmap image);

        void onFail(String url);
    }

    public interface ImageServiceListener {
        void onAllImageDownloadsFinish();
    }

}
