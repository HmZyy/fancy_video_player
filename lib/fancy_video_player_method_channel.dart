import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fancy_video_player_platform_interface.dart';

/// An implementation of [FancyVideoPlayerPlatform] that uses method channels.
class MethodChannelFancyVideoPlayer extends FancyVideoPlayerPlatform {
  /// The method channel used to interact with the native platform.
  Function? errorCallback;

  @visibleForTesting
  final methodChannel = const MethodChannel('fancy_video_player');

  @override
  Future<String?> startPlayer({
    required String url,
    Map<String, String>? headers,
    bool? autoPlay,
    bool closeOnError = false,
  }) async {
    methodChannel.setMethodCallHandler(_handleMethod);
    final result = await methodChannel.invokeMethod<String>('startPlayer', {
      "url": url,
      "headers": headers,
      "autoPlay": autoPlay,
      "closeOnError": closeOnError,
    });
    return result;
  }

  @override
  void setErrorCallback(Function callback) {
    errorCallback = callback;
  }

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "onPlayerError":
        int errorCode = call.arguments;
        errorCallback!(errorCode);
        break;
      default:
        print("Unknown method: ${call.method}");
    }
  }
}
