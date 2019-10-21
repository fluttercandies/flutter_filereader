package com.webview.filereader;

import android.content.Context;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;


public class X5FileReaderFactory extends PlatformViewFactory {


    private final BinaryMessenger messenger;
    private FlutterFileReaderPlugin plugin;
    private Context mContext;


    public X5FileReaderFactory(BinaryMessenger messenger, Context context,FlutterFileReaderPlugin plugin) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
        this.mContext = context;
        this.plugin = plugin;

    }

    @Override
    public PlatformView create(Context context, int i, Object args) {
        Map<String, Object> params = (Map<String, Object>) args;

        return new X5FileReaderView(mContext, messenger, i, params,plugin);
    }
}
