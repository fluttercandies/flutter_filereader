package com.webview.filereader;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterX5Plugin
 */
public class FlutterFileReaderPlugin implements MethodChannel.MethodCallHandler {

    private int x5LoadStatus = -1; // -1 未加载状态  5 成功 10 失败

    protected static final String channelName = "wv.io/FileReader";
    private MethodChannel methodChannel;
    private Handler mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                if (methodChannel != null) {
                    methodChannel.invokeMethod("onLoad", isLoadX5());
                }
            }
            return false;
        }
    });


    private FlutterFileReaderPlugin(Registrar registrar) {
        methodChannel = new MethodChannel(registrar.messenger(), channelName);
        methodChannel.setMethodCallHandler(this);
        initX5(registrar.context());
        netBroadcastRegister(registrar.context());
    }


    /**
     * Plugin registration.
     */
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
                if (!QbSdk.canLoadX5(context)) {
                    Log.d("FileReader", "网络变化->加载x5内核");
                    initX5(context);
                }
            }
        });
        //注册广播接收
        context.registerReceiver(netBroadcastReceiver, filter);


    }


    public void initX5(final Context context) {
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
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d("FileReader", "下载进度:" + i);
            }
        });

        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {

                Log.d("FileReader", "内核初始化结束");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                if (b) {
                    x5LoadStatus = 5;
                } else {
                    x5LoadStatus = 10;
                }
                Log.d("FileReader", "view初始化完成状态:" + b);
                Log.d("FileReader", "内核状态:" + QbSdk.canLoadX5(context));
                onX5LoadComplete();
            }
        });


    }

    @Override
    public void onMethodCall(MethodCall methodCall, final MethodChannel.Result result) {
        if ("isLoad".equals(methodCall.method)) {
            result.success(isLoadX5());
        }
    }


    private void onX5LoadComplete() {
        mainHandler.sendEmptyMessage(100);
    }


    int isLoadX5() {
        return x5LoadStatus;
    }
}

