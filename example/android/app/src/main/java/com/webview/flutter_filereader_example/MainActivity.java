package com.webview.flutter_filereader_example;

import android.os.Bundle;
import android.util.Log;

import io.flutter.embedding.android.FlutterActivity;


public class MainActivity extends FlutterActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.e("FileReader", "v2 初始化");
  }

}