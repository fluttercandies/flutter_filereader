# Flutter FileReader
[![pub package](https://img.shields.io/pub/v/flutter_filereader.svg)](https://pub.dartlang.org/packages/flutter_filereader)

##### A local file reader tool,Support a variety of file types, such as Doc Eexcl PPT TXT and so on,Android is implemented by tencent x5,iOS is implemented by WKWebView


## Depend on it
Add this to your package's pubspec.yaml file:

1.9.1
```
dependencies:
  flutter_filereader: ^1.0.0
```
1.12.x
```
dependencies:
  flutter_filereader: ^2.0.0
```


## Support File Type
* IOS `docx,doc,xlsx,xls,pptx,ppt,pdf,txt,jpg,jpeg,png`
* Android `docx,doc,xlsx,xls,pptx,ppt,pdf,txt`

## Usage

### iOS
Make sure you add the following key to Info.plist for iOS
```
<key>io.flutter.embedded_views_preview</key><true/>
```
 
### Example
```
import 'package:flutter/material.dart';
import 'package:flutter_filereader/flutter_filereader.dart';

class FileReaderPage extends StatefulWidget {
  final String filePath;

  FileReaderPage({Key: Key, this.filePath});

  @override
  _FileReaderPageState createState() => _FileReaderPageState();
}

class _FileReaderPageState extends State<FileReaderPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("doc"),
      ),
      body: FileReaderView(
        filePath: widget.filePath,
      ),
    );
  }
}
```


## 注意事项

1. Android端不支持x86和64位arm(x5不支持),解决办法参考[x5如何支持64位手机](https://x5.tencent.com/tbs/technical.html#/detail/sdk/1/34cf1488-7dc2-41ca-a77f-0014112bcab7 "x5如何支持64位手机")（demo中不需要处理,已经做了ABI过滤）
2. 因为问题1,所以在debug模式下,64位机器会显示x5内核加载不成功。主要是debug模式下,Flutter引擎会根据连接的机器打入对应的库,一但包含有arm-v8a,则无法加载x5内核所需的so库
3. 因为问题1,demo在64位机器上以Fluter项目模式运行或者`flutter run`会闪退。可以使用Android项目模式下直接运行
4. 为什么我本地Debug包可以正常加载内核，但是release包不可以？参考[x5混淆](https://x5.tencent.com/tbs/technical.html#/detail/sdk/1/c25c10b9-00a7-4fd8-99d9-46041f248226 "x5混淆")
5. Flutter 1.12.x版本开始使用Flutter build打包apk时会默认开启混淆,有可能导致引擎加载失败,demo中已经做了处理,库中也添加了混淆规则
6. txt文档如果显示乱码,请将txt文档编码改成gbk

