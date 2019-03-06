package com.webview.filereader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;


/**
 * Created by hanbin on 2017/9/12.
 */

public class NetBroadcastReceiver extends BroadcastReceiver {

    public NetChangeListener listener;

    NetBroadcastReceiver(NetChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // 如果相等的话就说明网络状态发生了变化
        Log.i("NetBroadcastReceiver", "NetBroadcastReceiver changed");
        if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = NetUtil.getNetWorkState(context);
            // 当网络发生变化，判断当前网络状态，并通过NetEvent回调当前网络状态
            if (listener != null) {
                listener.onChangeListener(netWorkState);
            }
        }
    }

    // 自定义接口
    public interface NetChangeListener {
        void onChangeListener(int status);
    }

}