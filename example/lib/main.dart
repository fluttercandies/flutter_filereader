import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_filereader_example/file.dart';
import 'package:path_provider/path_provider.dart';
import 'package:flutter_downloader/flutter_downloader.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: HomePage(),
    );
  }
}

String filePath =
    "https://upload.wikimedia.org/wikipedia/commons/6/60/The_Organ_at_Arches_National_Park_Utah_Corrected.jpg";

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String taskId;

  @override
  void initState() {
    super.initState();

    FlutterDownloader.registerCallback((id, status, progress) {
      print("状态$status");
      if (taskId == id) {
        print("下载进度$progress");
        if (status == DownloadTaskStatus.complete) {
          _findTaskById(taskId);
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: FlatButton(
              onPressed: () {
                _dowanload();
              },
              child: Text("下载")),
        ));
  }

  _dowanload() async {
    final tasks = await FlutterDownloader.loadTasksWithRawQuery(
        query: "select * from task where url = '$filePath'");

    if (tasks.length > 0) {
      final task = tasks.last;
      if (task.status == DownloadTaskStatus.complete) {
        final localPath = task.savedDir +
            "/" +
            task.url.substring(task.url.lastIndexOf("/") + 1);
        print("本地地址为 $localPath");

        if (await File(localPath).exists()) {
          Navigator.of(context).push(MaterialPageRoute(builder: (context) {
            return FileReaderPage(filePath: localPath);
          }));
          return;
        }
      }

      if (task.status == DownloadTaskStatus.running) {
        return;
      }
    }

    String savedPath = (await _localSavedDir()) + "/file";

    final dp = Directory(savedPath);
    bool exist = await dp.exists();
    if (!exist) {
      await dp.create();
    }

    print("下载路径$savedPath");
    taskId = await FlutterDownloader.enqueue(
        url: filePath,
        savedDir: savedPath,
        showNotification: false,
        openFileFromNotification: false);

    print("任务ID$taskId");
  }

  _findTaskById(String taskId) async {
    final tasks = await FlutterDownloader.loadTasksWithRawQuery(
        query: "select * from task where task_id = '$taskId'");

    if (tasks.length > 0) {
      final task = tasks.last;
      if (task.status == DownloadTaskStatus.complete) {
        final localPath = task.savedDir +
            "/" +
            task.url.substring(task.url.lastIndexOf("/") + 1);

        print("本地地址为 $localPath");
        if (await File(localPath).exists()) {
          Navigator.of(context).push(MaterialPageRoute(builder: (context) {
            return FileReaderPage(filePath: localPath);
          }));
          return;
        }
      }
    }
  }

  _localSavedDir() async {
    Directory dic;
    if (defaultTargetPlatform == TargetPlatform.android) {
      dic = await getExternalStorageDirectory();
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      dic = await getApplicationDocumentsDirectory();
    }
    return dic.path;
  }
}
