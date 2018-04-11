package study1.example.com.bitmapcache.imagemanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import study1.example.com.bitmapcache.MD5Encoder;

/**
 * Created by pengj on 2018-04-10.
 */

public class ImageUtil {
    private static ExecutorService threadPools;
    private LruCache<String, Bitmap> mCache;
    private Map<ImageView, Future<?>> mTasktags = new LinkedHashMap<ImageView, Future<?>>();//记录是否有线程正在对IamgeView进行处理
    private Context mContext;
    private Handler mHandler;

    public ImageUtil(Context mContext, Handler handler) {
        this.mContext = mContext;
        mHandler = handler;
        if (mCache == null) {
            mCache = new LruCache<String, Bitmap>((int) Runtime.getRuntime().freeMemory() / 4) {

                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getHeight() * value.getRowBytes();//重载大小计算方法
                }
            };

        }
        if (threadPools == null) {
            threadPools = Executors.newFixedThreadPool(4);
        }
    }

    public void display(ImageView iv, String mUrl) {
        Bitmap bitmap = mCache.get(mUrl);
        if (bitmap != null) {

            iv.setImageBitmap(bitmap);
            return;
        }
        bitmap = loadBitmapFromLocal(mUrl);
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
            return;
        }
        loadBitmapFromNet(iv, mUrl);
    }

    private Bitmap loadBitmapFromLocal(String mUrl) {
        String key;
        try {
            key = MD5Encoder.encode(mUrl);
            File file = new File(getCacheDir(), key);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                mCache.put(mUrl, bitmap);
                return bitmap;
            }
        } catch (Exception e) {

        }
        return null;
    }

    private String getCacheDir() {
        String state = Environment.getExternalStorageState();
        File dir = null;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 有sd卡
            dir = new File(Environment.getExternalStorageDirectory(), "/Android/data/" + mContext.getPackageName()
                    + "/icon");
        } else {
            // 没有sd卡
            dir = new File(mContext.getCacheDir(), "/icon");

        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath();
    }

    public void loadBitmapFromNet(ImageView iv, String url) {
        Future<?> future = mTasktags.get(iv);
        if (future != null && !future.isCancelled() && !future.isDone()) {
            Log.w("任务:", "取消创建");
            return;//停止创建新任务
//            future.cancel(true);//????
//            future = null;

        }
        future = threadPools.submit(new ImageLoadTask(iv, url));
        mTasktags.put(iv, future);

    }

    class ImageLoadTask implements Runnable {

        private String mUrl;
        private ImageView iv;

        public ImageLoadTask(ImageView iv, String url) {
            this.mUrl = url;
            this.iv = iv;
        }

        @Override
        public void run() {
            // HttpUrlconnection
            try {
                // 获取连接
                HttpURLConnection conn = (HttpURLConnection) new URL(mUrl).openConnection();

                conn.setConnectTimeout(30 * 1000);// 设置连接服务器超时时间
                conn.setReadTimeout(30 * 1000);// 设置读取响应超时时间

                // 连接网络
                conn.connect();

                // 获取响应码
                int code = conn.getResponseCode();

                if (200 == code) {
                    InputStream is = conn.getInputStream();

                    // 将流转换为bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    // 存储到本地
                    write2Local(mUrl, bitmap);
                    // 存储到内存
                    mCache.put(mUrl, bitmap);


                    // 图片显示:不可取
                    // iv.setImageBitmap(bitmap);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // iv.setImageBitmap(bitmap);

                            display(iv, mUrl);
                        }
                    });
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void write2Local(String mUrl, Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            String md5Name = MD5Encoder.encode(mUrl);
            File file = new File(getCacheDir(), md5Name);
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                    fileOutputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
