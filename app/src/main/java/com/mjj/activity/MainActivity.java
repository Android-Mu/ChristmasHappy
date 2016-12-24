package com.love.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.love.R;
import com.love.factory.ImageNameFactory;
import com.love.util.DeviceInfo;
import com.love.util.ImageUtils;
import com.love.view.bluesnow.FlowerView;
import com.love.view.heart.HeartLayout;
import com.love.view.typewriter.TypeTextView;
import com.love.view.whitesnow.SnowView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

    private static final String JPG = ".jpg";
    private static final String LOVE = "2016接近尾声，请给忙碌的自己道声平安，平安夜来了，送上悄悄地祝福，一生平安，圣诞快乐，来年好运！";
    private static final int SNOW_BLOCK = 1;
    public static final String URL = "file:///android_asset/index.html";
    private Canvas mCanvas;
    private int mCounter;

    private FrameLayout root_fragment_layout = null; // 根布局
    private FrameLayout mWebViwFrameLayout = null; // 背景图上面的布局
    private ImageView mImageView;// 中间的图片
    private SnowView mWhiteSnowView; // 刚开始的白色雪花
    private HeartLayout mHeartLayout; // 右侧垂直方向的漂浮红心
    private FlowerView mBlueSnowView; // 蓝色的雪花
    private TypeTextView mTypeTextView; // 雪花完了之后显示文字的控件
    private Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            MainActivity.this.mBlueSnowView.inva();
        }
    };
    private Bitmap mManyBitmapSuperposition;
    private Random mRandom = new Random();
    private Random mRandom2 = new Random();
    private TimerTask mTask = null;
    private WebSettings mWebSettings;
    private WebView mWebView;
    private Timer myTimer = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceInfo.getInstance().initializeScreenInfo(this);

        setContentView(R.layout.main);
        initView();
        initWebView();
        // 雪花完了之后就去加载WebView
        delayShowAll(3000L);
    }

    private void initWebView() {
        this.mWebSettings = this.mWebView.getSettings();
        this.mWebSettings.setJavaScriptEnabled(true);
        this.mWebSettings.setBuiltInZoomControls(false);
        this.mWebSettings.setLightTouchEnabled(false);
        this.mWebSettings.setSupportZoom(false);
        this.mWebView.setHapticFeedbackEnabled(false);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mWebViwFrameLayout = (FrameLayout) findViewById(R.id.fl_webView_layout);
        root_fragment_layout = (FrameLayout) findViewById(R.id.root_fragment_layout);
        this.mWebView = new WebView(getApplicationContext());
        this.mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        this.mWebView.setVisibility(View.GONE);

        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fp.gravity = Gravity.CENTER;

        mWebViwFrameLayout.addView(mWebView);

        this.mHeartLayout = (HeartLayout) findViewById(R.id.heart_o_red_layout);
        this.mTypeTextView = (TypeTextView) findViewById(R.id.typeTextView);
        this.mWhiteSnowView = (SnowView) findViewById(R.id.whiteSnowView);
        this.mImageView = (ImageView) findViewById(R.id.image);
        this.mBlueSnowView = (FlowerView) findViewById(R.id.flowerview);
        this.mBlueSnowView.setWH(DeviceInfo.mScreenWidthForPortrait, DeviceInfo.mScreenHeightForPortrait, DeviceInfo.mDensity);
        this.mBlueSnowView.loadFlower();
        this.mBlueSnowView.addRect();
        this.myTimer = new Timer();

        // 发消息白色雪花
        this.mTask = new TimerTask() {
            public void run() {
                Message msg = new Message();
                msg.what = MainActivity.SNOW_BLOCK;
                MainActivity.this.mHandler.sendMessage(msg);
            }
        };
        // 从assets文件夹下初始化背景图片
        rxJavaSolveMiZhiSuoJinAndNestedLoopAndCallbackHell();

        this.myTimer.schedule(this.mTask, 5200, 10); // 设置白色雪花的定时

        // 中间文字显示完之后开始雪花飘落
        this.mTypeTextView.setOnTypeViewListener(new TypeTextView.OnTypeViewListener() {
            public void onTypeStart() {
                // 每次开始设置随机颜色
                MainActivity.this.mTypeTextView.setTextColor(randomColor());
            }

            public void onTypeOver() {
                delayShowTheSnow();
            }
        });
        this.mTypeTextView.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (this.mWebView != null) {
            if (mWebViwFrameLayout != null) {
                this.mWebViwFrameLayout.removeAllViewsInLayout();
                this.mWebViwFrameLayout.removeAllViews();
            }
            this.mWebView.removeAllViews();
            this.mWebView.destroy();
            this.mWebView = null;
        }
        unBindDrawables(findViewById(R.id.root_fragment_layout));
        System.gc();
    }

    private void cancelTimer() {
        if (this.myTimer != null) {
            this.myTimer.cancel();
            this.myTimer = null;
        }
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask = null;
        }
    }

    private void createSingleImageFromMultipleImages(Bitmap bitmap, int mCounter) {
        if (mCounter == 0) {
            try {
                this.mManyBitmapSuperposition = Bitmap.createBitmap(DeviceInfo.mScreenWidthForPortrait,
                        DeviceInfo.mScreenHeightForPortrait, bitmap.getConfig());
                this.mCanvas = new Canvas(this.mManyBitmapSuperposition);
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
                System.gc();
            } finally {

            }
        }
        if (this.mCanvas != null) {
            int number = DeviceInfo.mScreenHeightForPortrait / 64;
            if (mCounter >= (mCounter / number) * number && mCounter < ((mCounter / number) + SNOW_BLOCK) * number) {
                this.mCanvas.drawBitmap(bitmap, (float) ((mCounter / number) * 64), (float) ((mCounter % number) * 64), null);
            }
        }
    }

    private void rxJavaSolveMiZhiSuoJinAndNestedLoopAndCallbackHell() {
        Observable.from(ImageNameFactory.getAssetImageFolderName())
                .flatMap(new Func1<String, Observable<String>>() {
                    public Observable<String> call(String folderName) {
                        return Observable.from(ImageUtils.getAssetsImageNamePathList(MainActivity.this.getApplicationContext(), folderName));
                    }
                }).filter(new Func1<String, Boolean>() {
            public Boolean call(String imagePathNameAll) {
                return Boolean.valueOf(imagePathNameAll.endsWith(MainActivity.JPG));
            }
        }).map(new Func1<String, Bitmap>() {
            public Bitmap call(String imagePathName) {
                return ImageUtils.getImageBitmapFromAssetsFolderThroughImagePathName(MainActivity.this.getApplicationContext(),
                        imagePathName, DeviceInfo.mScreenWidthForPortrait, DeviceInfo.mScreenHeightForPortrait);
            }
        }).map(new Func1<Bitmap, Void>() {
            public Void call(Bitmap bitmap) {
                MainActivity.this.createSingleImageFromMultipleImages(bitmap, MainActivity.this.mCounter);
                MainActivity.this.mCounter = MainActivity.this.mCounter++;
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    public void call() {

                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    public void onCompleted() {
                        MainActivity.this.mImageView.setImageBitmap(MainActivity.this.mManyBitmapSuperposition);
                        MainActivity.this.showAllViews();
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Void aVoid) {
                    }
                });
    }

    /**
     * 显示背景图和白色雪花
     */
    private void showAllViews() {
        this.mImageView.setVisibility(View.VISIBLE);
        this.mWhiteSnowView.setVisibility(View.VISIBLE);
    }

    /**
     * 加载WebView
     */
    private void gotoNext() {
        delayDo();
    }

    /**
     * 显示中间的文字
     *
     * @param time 延迟时间
     */
    private void delayShow(long time) {
        Observable.timer(time, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    public void onCompleted() {
                        MainActivity.this.mTypeTextView.start(MainActivity.LOVE);
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Long aLong) {
                    }
                });
    }

    /**
     * 第二次雪花飘落,在文字显示完成之后进行
     */
    private void delayShowTheSnow() {
        Observable.timer(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    public void onCompleted() {
                        // 隐藏掉原来的白色雪花,右侧红心开心出现
                        mBlueSnowView.setVisibility(View.VISIBLE);
                        MainActivity.this.showRedHeartLayout();
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Long aLong) {
                    }
                });
    }

    private void delayDo() {
        Observable.timer(0, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    public void onCompleted() {
                        MainActivity.this.mWhiteSnowView.setVisibility(View.GONE);
                        MainActivity.this.mWebView.setVisibility(View.VISIBLE);
                        mWebView.setBackgroundColor(randomColor()); // 设置WebView背景色
                        MainActivity.this.delayShow(0L); // 延时显示显示打印机
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Long aLong) {
                    }
                });
    }

    private void delayShowAll(long time) {
        Observable.timer(time, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    public void onCompleted() {
                        MainActivity.this.gotoNext();
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Long aLong) {
                    }
                });
    }

    /**
     * 右侧红心使用的随机颜色
     *
     * @return
     */
    private int randomColor() {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
    }

    /**
     * 初始化右侧红心
     */
    private void showRedHeartLayout() {
        Observable.timer(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    public void onCompleted() {
                        // 显示右侧红心,并开启循环模式
                        mHeartLayout.setVisibility(View.VISIBLE);
                        MainActivity.this.delayDo2();
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Long aLong) {
                    }
                });
    }

    /**
     * 右侧红心循环出现
     */
    private void delayDo2() {
        Observable.timer((long) this.mRandom2.nextInt(200), TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    public void onCompleted() {
                        MainActivity.this.mHeartLayout.addHeart(MainActivity.this.randomColor());
                        MainActivity.this.delayDo2();
                    }

                    public void onError(Throwable e) {
                    }

                    public void onNext(Long aLong) {

                    }
                });
    }

    /**
     * remove View Drawables
     *
     * @param view
     */
    private void unBindDrawables(View view) {
        if (view != null) {
            try {
                Drawable drawable = view.getBackground();
                if (drawable != null) {
                    drawable.setCallback(null);
                } else {
                }
                if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int viewGroupChildCount = viewGroup.getChildCount();
                    for (int j = 0; j < viewGroupChildCount; j++) {
                        unBindDrawables(viewGroup.getChildAt(j));
                    }
                    viewGroup.removeAllViews();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
