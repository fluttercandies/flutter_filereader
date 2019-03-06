import 'package:flutter/foundation.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

//var par = Map();
//par["filePath"] = "/storage/emulated/0/007/test.xlsx";

class FileReaderView extends StatefulWidget {
  final String filePath; //需要是本地的路径

  FileReaderView({Key key, this.filePath});

  @override
  _FileReaderViewState createState() => _FileReaderViewState();
}

class _FileReaderViewState extends State<FileReaderView> {
  static const MethodChannel _channel = const MethodChannel('wv.io/FileReader');

  int _status = 0; //0 loading 5,不支持的文件,10 显示

  @override
  void initState() {
    super.initState();

    _methodChannel();
  }

  _methodChannel() {
    _channel.invokeMethod("isLoad").then((onValue) {
      if (onValue) {
        _setStatus(10);
      } else {
        _channel.setMethodCallHandler((call) {
          if (call.method == "onLoad") {
            _setStatus(10);
          }
        });
      }
    });
  }

  _setStatus(int status) {
    _status = status;
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android ||
        defaultTargetPlatform == TargetPlatform.iOS) {
      if (_status == 0) {
        return _loadingWidget();
      } else if (_status == 5) {
        return _unSupportFile();
      } else if (_status == 10) {
        if (defaultTargetPlatform == TargetPlatform.android) {
          return _createAndroidView();
        } else {
          return _createIosView();
        }
      } else {
        return _loadingWidget();
      }
    } else {
      return Center(child: Text("不支持的平台"));
    }
  }

  Widget _unSupportFile() {
    return Center(
      child: Text("不支持打开${fileType(widget.filePath)}类型的文件"),
    );
  }

  Widget _loadingWidget() {
    return Center(
      child: CupertinoActivityIndicator(),
    );
  }

  Widget _createAndroidView() {
    return AndroidView(
        viewType: "FileReader",
        onPlatformViewCreated: _onPlatformViewCreated,
        creationParamsCodec: StandardMessageCodec());
  }

  _onPlatformViewCreated(int id) {
    MethodChannel('wv.io/FileReader' + "_$id")
        .invokeMethod("openFile", widget.filePath)
        .then((openSuccess) {
      if (!openSuccess) {
        _setStatus(5);
      }
    });
  }

  Widget _createIosView() {
    return UiKitView(
      viewType: "FileReader",
      onPlatformViewCreated: _onPlatformViewCreated,
      creationParamsCodec: StandardMessageCodec(),
    );
  }

  String fileType(String filePath) {
    if (filePath == null || filePath.isEmpty) {
      return "";
    }
    int i = filePath.lastIndexOf('.');
    if (i <= -1) {
      return "";
    }
    return filePath.substring(i + 1);
  }
}
