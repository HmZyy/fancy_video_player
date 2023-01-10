import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fancy_video_player_platform_interface.dart';

/// An implementation of [FancyVideoPlayerPlatform] that uses method channels.
class MethodChannelFancyVideoPlayer extends FancyVideoPlayerPlatform {
  /// The method channel used to interact with the native platform.
  Function? errorCallback;
  Function? onErrorBoxClicked;
  Function? onBackPressed;
  Function? onEnterPictureInPicture;

  @visibleForTesting
  final methodChannel = const MethodChannel('fancy_video_player');

  @override
  Future<String?> startPlayer({
    required String url,
    Map<String, String>? headers,
    bool? autoPlay,
    bool closeOnError = false,
    bool showErrorBox = false,
  }) async {
    methodChannel.setMethodCallHandler(_handleMethod);
    final result = await methodChannel.invokeMethod<String>('startPlayer', {
      "url": url,
      "headers": headers,
      "autoPlay": autoPlay,
      "closeOnError": closeOnError,
      "showErrorBox": showErrorBox,
    });
    return result;
  }

  @override
  void setErrorCallback(Function callback) {
    errorCallback = callback;
  }

  @override
  void setOnErrorBoxClicked(Function callback) {
    onErrorBoxClicked = callback;
  }

  @override
  void setOnBackPressed(Function callback) {
    onBackPressed = callback;
  }

  @override
  void setOnEnterPictureInPicture(Function callback) {
    onEnterPictureInPicture = callback;
  }

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "onPlayerError":
        int errorCode = call.arguments;
        errorCallback!(errorCode);
        break;
      case "onErrorBoxClicked":
        onErrorBoxClicked!();
        break;
      case "onBackPressed":
        onBackPressed!();
        break;
      case "onEnterPictureInPicture":
        onEnterPictureInPicture!();
        break;
      default:
        print("Unknown method: ${call.method}");
    }
  }
}
