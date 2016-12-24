
package com.love.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 类描述:图片处理类
 */
public class ImageUtils {

    /**
     * 从assets文件夹中获取制定路径的图片的Bitmap
     *
     * @param context:上下文
     * @param imagePathName:图片路径的名称:例如: paowuxian/04.jpg
     * @param reqWidth:图片需要显示的宽
     * @param reqHeight:图片需要显示的高
     * @return
     */
    public static Bitmap getImageBitmapFromAssetsFolderThroughImagePathName(Context context, String imagePathName, int reqWidth, int reqHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        AssetManager assetManager = context.getResources().getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open(imagePathName);
            inputStream.mark(Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
            return null;
        }

        try {
            if (inputStream != null) {
                BitmapFactory.decodeStream(inputStream, null, opts);
                inputStream.reset();
            } else {
                return null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        opts.inSampleSize = calculateInSampleSiez(opts, reqWidth, reqHeight);
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        opts.inDither = false;
        opts.inTempStorage = new byte[512 * 1024];
        try {
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
            } else {
                return null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        if (bitmap != null) {
            try {
                bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
                System.gc();
                return null;
            }
        }
        return bitmap;
    }

    /**
     * 获取一个合适的缩放系数(2^n)
     *
     * @param options:Options
     * @param reqWidth:图片需要显示的宽
     * @param reqHeight:图片需要显示的高
     * @return 缩放系数, 2的次方
     */
    public static int calculateInSampleSiez(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 获取assets文件夹下某个文件夹中所有图片路径的集合
     * <p/>
     * 例如:assets/dongyu 这个目录下的图片路径的集合
     * {
     * dongyu/linzhengxin.jpg
     * }
     *
     * @param context:上下文
     * @param folderName:文件夹名称
     * @return
     */
    public static ArrayList<String> getAssetsImageNamePathList(Context context, String folderName) {
        ArrayList<String> imagePathList = new ArrayList<String>();
        String[] imageNameArray = getAssetsImageNameArray(context, folderName);

        if (imageNameArray != null && imageNameArray.length > 0 && folderName != null && !folderName.replaceAll(" ", "").trim().equals("")) {
            for (String imageName : imageNameArray) {
                imagePathList.add(new StringBuffer(folderName).append(File.separator).append(imageName).toString());
            }
        }

        return imagePathList;
    }

    /**
     * 得到assets文件夹下--某个文件夹下--所有文件的文件名
     * 例如:assets/dongyu 这个目录下的图片名称(包含后缀)集合
     * {
     * linzhengxin.jpg,
     * }
     *
     * @param context:上下文
     * @param folderName:文件夹名称
     * @return
     */
    private static String[] getAssetsImageNameArray(Context context, String folderName) {
        String[] imageNameArray = null;
        try {
            imageNameArray = context.getAssets().list(folderName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageNameArray;
    }

    /**
     * 将本地ResFolder图片转换为Bitmap
     */
    public static Bitmap getLocalBitmapFromResFolder(Context context, int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

    /**
     * 获取Bitmap大小
     *
     * @param bitmap
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static int getBitmapSize2(Bitmap bitmap) {
        if (bitmap == null) return 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getRowBytes() * bitmap.getHeight();
        } else {
            return bitmap.getByteCount();
        }
    }

    /**
     * 获取Bitmap大小
     *
     * @param bitmap
     * @return
     */
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {// API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight(); // earlier version
    }
}
