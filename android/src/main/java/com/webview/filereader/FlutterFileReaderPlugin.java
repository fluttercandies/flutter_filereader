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
import com.tencent.smtt.sdk.ValueCallback;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.embedding.engine.plugins.FlutterPlugin;


/**
 * FlutterX5Plugin
 */
public class FlutterFileReaderPlugin implements FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler {

    private int x5LoadStatus = -1; // -1 未加载状态  5 成功 10 失败

    private static String TAG = "FileReader";

    public static final String channelName = "wv.io/FileReader";
    private FlutterPluginBinding flutterPluginBinding;

    private Context ctx;
    private Context activityContext;
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


    public FlutterFileReaderPlugin(){
        Log.d(TAG,"init");
    }

    private void loadParameters(FlutterPluginBinding binding) {
        ctx = binding.getApplicationContext();
        methodChannel = new MethodChannel(binding.getBinaryMessenger(), channelName);
        methodChannel.setMethodCallHandler(this);
        initX5(binding.getApplicationContext());
        netBroadcastRegister(binding.getApplicationContext());
    }


    public void netBroadcastRegister(final Context context) {
        //实例化IntentFilter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        NetBroadcastReceiver netBroadcastReceiver = new NetBroadcastReceiver(new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (!QbSdk.canLoadX5(context)) {
                  //  Log.d(TAG, "网络变化->加载x5内核");
                    initX5(context);
                }
            }
        });
        //注册广播接收
        context.registerReceiver(netBroadcastReceiver, filter);
    }


    public void initX5(final Context context) {
        Log.d(TAG,"初始化X5->"+QbSdk.canLoadX5(context));
        if(!QbSdk.canLoadX5(context)){
            //重要
            QbSdk.reset(context);
        }
        // 在调用TBS初始化、创建WebView之前进行如下配置，以开启优化方案
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setNeedInitX5FirstTime(true);
        QbSdk.setDownloadWithoutWifi(true);

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d(TAG, "TBS下载完成");
            }

            @Override
            public void onInstallFinish(int i) {

            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d(TAG, "TBS下载进度:" + i);
            }
        });

        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.d(TAG, "TBS内核初始化结束");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                if (b) {
                    x5LoadStatus = 5;
                } else {
                    x5LoadStatus = 10;
                }
               // Log.d(TAG, "view初始化完成状态:" + b);
                Log.d(TAG, "TBS内核状态:" + QbSdk.canLoadX5(context));
                onX5LoadComplete();
            }
        });

    }

    @Override
    public void onMethodCall(MethodCall methodCall, final MethodChannel.Result result) {
        if ("isLoad".equals(methodCall.method)) {
            result.success(isLoadX5());
        }else if("openFileByMiniQb".equals(methodCall.method)){
            String filePath = (String) methodCall.arguments;
            result.success(openFileByMiniQb(filePath));
        }
    }

    public boolean openFileByMiniQb(String filePath){

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("style", "1");
        params.put("local", "false");

        QbSdk.openFileReader(activityContext, filePath, params, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.d(TAG,"openFileReader->"+s);
            }
        });

        return true;
    }


    private void onX5LoadComplete() {
        mainHandler.sendEmptyMessage(100);
    }


    int isLoadX5() {
        if(QbSdk.canLoadX5(ctx)){
         //   Log.d(TAG,"x5 is Load");
            x5LoadStatus = 5;
        }
        return x5LoadStatus;
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        this.flutterPluginBinding = binding;
//
        Log.d(TAG, "onAttachedToEngine");
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        Log.d(TAG, "onDetachedFromEngine");
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        this.activityContext = binding.getActivity();
        this.loadParameters(this.flutterPluginBinding);
        this.flutterPluginBinding.getPlatformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(this.flutterPluginBinding.getBinaryMessenger(), this.activityContext, this));
        Log.d(TAG, "onAttachedToActivity");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.d(TAG, "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        Log.d(TAG, "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.d(TAG, "onDetachedFromActivity");
        activityContext = null;
    }
}

