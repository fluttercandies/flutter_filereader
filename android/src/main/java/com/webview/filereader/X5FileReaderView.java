package com.webview.filereader;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class X5FileReaderView implements PlatformView, MethodChannel.MethodCallHandler, TbsReaderView.ReaderCallback {
    private MethodChannel methodChannel;
    private TbsReaderView readerView;

    private String tempPath;


    FlutterFileReaderPlugin plugin;


    X5FileReaderView(Context context, BinaryMessenger messenger, int id, Map<String, Object> params, FlutterFileReaderPlugin plugin) {
        this.plugin = plugin;
        tempPath = context.getCacheDir() + "/" + "TbsReaderTemp";
        methodChannel = new MethodChannel(messenger, FlutterFileReaderPlugin.channelName + "_" + id);
        methodChannel.setMethodCallHandler(this);
        //这里的Context需要Activity
        readerView = new TbsReaderView(context, this);
        readerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {

        switch (methodCall.method) {
            case "openFile":
                if (isSupportFile((String) methodCall.arguments)) {
                    openFile((String) methodCall.arguments);
                    result.success(true);
                } else {
                    //    plugin.openFileByMiniQb((String) methodCall.arguments);
                    result.success(false);
                }
                break;
            case "canOpen":
                result.success(isSupportFile((String) methodCall.arguments));
                break;


        }
    }


    void openFile(String filePath) {
        if (isSupportFile(filePath)) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            File bsReaderTempFile = new File(tempPath);
            if (!bsReaderTempFile.exists()) {
                bsReaderTempFile.mkdir();
            }
            //加载文件
            Bundle localBundle = new Bundle();
            localBundle.putString("filePath", filePath);
            localBundle.putBoolean("is_bar_show", false);
            localBundle.putBoolean("menu_show", false);
            localBundle.putBoolean("is_bar_animating", false);
            localBundle.putString("tempPath", tempPath);
            readerView.openFile(localBundle);
        }

    }


    boolean isSupportFile(String filePath) {
        return readerView.preOpen(getFileType(filePath), false);
    }

    /***
     * 获取文件类型
     *
     * @param paramString
     * @return
     */
    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {

            return str;
        }

        int i = paramString.lastIndexOf('.');
        if (i <= -1) {

            return str;
        }
        str = paramString.substring(i + 1);
        return str;
    }

    @Override
    public View getView() {
        return readerView;
    }

    @Override
    public void dispose() {
        Log.d("FileReader", "FileReader Close");
        readerView.onStop();
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        readerView = null;
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }
}
