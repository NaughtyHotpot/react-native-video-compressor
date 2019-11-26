package com.react.videocompressor;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.vincent.videocompressor.VideoCompress;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoCompressorModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Promise mCompressPromise;

    private static final int COMPRESS_QUALITY_HIGH = 1;
    private static final int COMPRESS_QUALITY_MEDIUM = 2;
    private static final int COMPRESS_QUALITY_LOW = 3;
    private int compressQuality = 3;

    public VideoCompressorModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private void setConfiguration(final ReadableMap options) {
        compressQuality = options.hasKey("compressQuality") ? options.getInt("compressQuality") : compressQuality;
    }

    @Override
    public String getName() {
        return "VideoCompressor";
    }

    @ReactMethod
    public void compress(String path, ReadableMap options, Promise promise) {
        setConfiguration(options);
        mCompressPromise = promise;
        final String destPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";
        VideoCompress.CompressListener listener = new VideoCompress.CompressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess() {
                WritableMap resultArr = Arguments.createMap();
                resultArr.putString("path", destPath);
                mCompressPromise.resolve(resultArr);
            }

            @Override
            public void onFail() {
                mCompressPromise.reject("0","Compress Failed.");
            }

            @Override
            public void onProgress(float percent) {

            }
        };
        if(path!=null){
            String inPath = path;
            if(path.startsWith("file://")){
                inPath = path.replaceFirst("file://", "");
            }
            compressVideo(inPath, destPath, listener);
        }else {
            mCompressPromise.reject("0","Compress Failed.");
        }



    }

    private void compressVideo(String path, String destPath, VideoCompress.CompressListener listener){
//        String destPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";
        switch (compressQuality) {
            default:
            case COMPRESS_QUALITY_HIGH:
                VideoCompress.compressVideoHigh(path,destPath,listener);
                break;
            case COMPRESS_QUALITY_MEDIUM:
                VideoCompress.compressVideoMedium(path,destPath,listener);
                break;
            case COMPRESS_QUALITY_LOW:
                VideoCompress.compressVideoLow(path,destPath,listener);
                break;
        }

    }

    private Locale getLocale() {
        Configuration config = reactContext.getResources().getConfiguration();
        Locale sysLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysLocale = getSystemLocale(config);
        } else {
            sysLocale = getSystemLocaleLegacy(config);
        }

        return sysLocale;
    }

    private static Locale getSystemLocaleLegacy(Configuration config){
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Locale getSystemLocale(Configuration config){
        return config.getLocales().get(0);
    }
}
