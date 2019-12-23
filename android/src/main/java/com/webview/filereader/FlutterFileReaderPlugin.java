package com.webview.filereader;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.ValueCallback;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterX5Plugin
 */
public class FlutterFileReaderPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {

    private int x5LoadStatus = -1; // -1 未加载状态  5 成功 10 失败

    public static final String channelName = "wv.io/FileReader";
    private Context ctx;
    private MethodChannel methodChannel;
    private NetBroadcastReceiver netBroadcastReceiver;
    private FlutterPluginBinding pluginBinding;

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


    private void init(Context context, BinaryMessenger messenger) {
        ctx = context;
        methodChannel = new MethodChannel(messenger, channelName);
        methodChannel.setMethodCallHandler(this);
        initX5(context);
        netBroadcastRegister(context);
    }

    public FlutterFileReaderPlugin() {

    }

    private void onDestory() {
        if (netBroadcastReceiver != null && ctx != null) {
            ctx.unregisterReceiver(netBroadcastReceiver);
        }
        ctx = null;
        methodChannel = null;
        pluginBinding = null;
    }


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        FlutterFileReaderPlugin plugin = new FlutterFileReaderPlugin();
        plugin.init(registrar.context(), registrar.messenger());
        registrar.platformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(registrar.messenger(), registrar.activity(), plugin));
    }


    public void netBroadcastRegister(final Context context) {
        //实例化IntentFilter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcastReceiver = new NetBroadcastReceiver(new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (x5LoadStatus != 5) {
                    initX5(context);
                }
            }
        });
        //注册广播接收
        context.registerReceiver(netBroadcastReceiver, filter);


    }


    public void initX5(final Context context) {
        Log.e("FileReader", "初始化X5");
        if (!QbSdk.canLoadX5(context)) {
            //重要
            QbSdk.reset(context);
        }
        // 在调用TBS初始化、创建WebView之前进行如下配置，以开启优化方案
        HashMap<String, Object> map = new HashMap<String, Object>();
      //  map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
       // map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
       // QbSdk.initTbsSettings(map);
        QbSdk.setNeedInitX5FirstTime(true);
        QbSdk.setDownloadWithoutWifi(true);


        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.e("FileReader", "TBS下载完成");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.e("FileReader", "TBS安装完成");
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.e("FileReader", "TBS下载进度:" + i);
            }
        });

        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.e("FileReader", "TBS内核初始化结束");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                if (b) {
                    x5LoadStatus = 5;
                } else {
                    x5LoadStatus = 10;
                }
                Log.e("FileReader", "TBS内核状态:" + b + "--" + QbSdk.canLoadX5(context));
                onX5LoadComplete();
            }
        });


    }

    @Override
    public void onMethodCall(MethodCall methodCall, final MethodChannel.Result result) {
        if ("isLoad".equals(methodCall.method)) {
            result.success(isLoadX5());
        } else if ("openFileByMiniQb".equals(methodCall.method)) {
            String filePath = (String) methodCall.arguments;
            result.success(openFileByMiniQb(filePath));
        }
    }

    public boolean openFileByMiniQb(String filePath) {
        if (ctx != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("style", "1");
            params.put("local", "false");
            QbSdk.openFileReader(ctx, filePath, params, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.d("FileReader", "openFileReader->" + s);
                }
            });
        }
        return true;
    }


    private void onX5LoadComplete() {
        mainHandler.sendEmptyMessage(100);
    }


    int isLoadX5() {
        if (ctx != null && QbSdk.canLoadX5(ctx)) {
            x5LoadStatus = 5;
        }
        return x5LoadStatus;
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        Log.e("FileReader", "onAttachedToEngine");

        pluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        Log.e("FileReader", "onDetachedFromEngine");
        onDestory();
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        Log.e("FileReader", "onAttachedToActivity");
        FlutterFileReaderPlugin plugin = new FlutterFileReaderPlugin();
        plugin.init(pluginBinding.getApplicationContext(), pluginBinding.getBinaryMessenger());
        pluginBinding.getPlatformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(pluginBinding.getBinaryMessenger(), binding.getActivity(), plugin));
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.e("FileReader", "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        Log.e("FileReader", "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.e("FileReader", "onDetachedFromActivity");
    }
}

