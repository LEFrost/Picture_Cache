package study1.example.com.bitmapcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import study1.example.com.bitmapcache.imagemanager.ImageUtil;

/**
 * Created by pengj on 2018-04-09.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<String> imageUri;
    Handler handler;
    Context mContext;
    ImageUtil imageUtil;

    public ImageAdapter(List<String> list, Handler handler, Context context) {
        mContext = context;
        imageUri = list;
        imageUtil = new ImageUtil(mContext, handler);
        this.handler = handler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.imageView = (ImageView) view.findViewById(R.id.image_item);
        ViewGroup.LayoutParams params = ((ImageView) holder.imageView).getLayoutParams();
        Random random = new Random(System.currentTimeMillis());
        if (!holder.fristLoad) {
            holder.fristLoad = true;
            params.height = random.nextInt(600) + 400;
            params.width = random.nextInt(500) + 350;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final ImageAdapter.ViewHolder holder, final int position) {
//        Bitmap bitmap=getBitmap(imageUri.get(position));
        final ImageView imageView = (ImageView) holder.imageView;

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    final Bitmap bitmap = getBitmap(imageUri.get(position));
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            imageView.setImageBitmap(bitmap);
//                        }
//                    });
//                }
//            }).start();
        imageUtil.display(imageView, imageUri.get(position));

    }

    private Bitmap getBitmap(String uri) {
        URL url = null;
        Bitmap bitmap = null;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return imageUri.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        boolean fristLoad = false;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
