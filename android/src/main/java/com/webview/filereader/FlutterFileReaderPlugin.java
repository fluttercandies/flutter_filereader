package com.webview.filereader;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterX5Plugin */
public class FlutterFileReaderPlugin implements MethodChannel.MethodCallHandler {

    private boolean isFirst = false;

    protected static final String channelName = "wv.io/FileReader";
    private MethodChannel methodChannel;
    private Context context;


    private FlutterFileReaderPlugin(Registrar registrar) {
        this.context = registrar.context();
        methodChannel = new MethodChannel(registrar.messenger(), channelName);
        methodChannel.setMethodCallHandler(this);
        netBroadcastRegister(registrar.context());

    }


    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
        new FlutterFileReaderPlugin(registrar);
        registrar.platformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(registrar.messenger(), registrar.activity()));

    }


    public void netBroadcastRegister(final Context context) {
        //实例化IntentFilter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        NetBroadcastReceiver netBroadcastReceiver = new NetBroadcastReceiver(new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (isFirst && !QbSdk.canLoadX5(context)) {
                    isFirst = false;
                    initX5(context);
                    return;
                }
                if (!QbSdk.canLoadX5(context)) {
                    Log.d("FileReader", "网络变化");
                    initX5(context);
                }
            }
        });
        //注册广播接收
        context.registerReceiver(netBroadcastReceiver, filter);


    }


    public void initX5(Context context) {
        // 在调用TBS初始化、创建WebView之前进行如下配置，以开启优化方案
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setNeedInitX5FirstTime(true);
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d("FileReader", "下载完成");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.d("FileReader", "安装完成");
                onX5LoadComplete();
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d("FileReader", "下载进度:" + i);
            }
        });

        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {

                Log.d("FileReader", "内核初始化完成");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.d("FileReader", "view初始化完成状态:" + b);
                onX5LoadComplete();
            }
        });


    }

    @Override
    public void onMethodCall(MethodCall methodCall, final MethodChannel.Result result) {
        switch (methodCall.method) {
            case "isLoad":
                result.success(isLoadX5(context));
                break;

        }
    }


    private void onX5LoadComplete() {
        if (methodChannel != null) {
            methodChannel.invokeMethod("onLoad", isLoadX5(context));
        }

    }


    boolean isLoadX5(Context context) {
        return QbSdk.canLoadX5(context);
    }
}

