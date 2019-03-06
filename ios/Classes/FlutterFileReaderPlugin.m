#import "FlutterFileReaderPlugin.h"
#import <flutter_filereader/flutter_filereader-Swift.h>

@implementation FlutterFileReaderPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterFileReaderPlugin registerWithRegistrar:registrar];
}
@end
