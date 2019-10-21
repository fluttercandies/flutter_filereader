import Flutter

import UIKit


let supportFileType = ["docx","doc","xlsx","xls","pptx","ppt","pdf","txt","jpg","jpeg","png"]

public func isSupportOpen(fileType:String) -> Bool {
    if supportFileType.contains(fileType.lowercased()) {
        return true
    }
    return false
}

public func fileType(filePath:String?) -> String {
    var str = ""
    if filePath == nil {
        return str
    }
    if filePath!.isEmpty {
        return str
    }
    
    if let i = filePath!.lastIndex(of: ".") {
        str = filePath!.substring(from: String.Index.init(encodedOffset: (i.encodedOffset + 1)))
    }
    return str
    
}


public class SwiftFlutterFileReaderPlugin: NSObject, FlutterPlugin {
    
    static let channelName = "wv.io/FileReader"
    
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: channelName, binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterFileReaderPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    
    registrar.register(FileReaderFactory.init(messenger: registrar.messenger()), withId: "FileReader")
    
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
    
    if call.method == "isLoad" {
        result(5)
        return
    }
  }
    
}
