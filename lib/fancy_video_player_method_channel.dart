import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fancy_video_player_platform_interface.dart';

/// An implementation of [FancyVideoPlayerPlatform] that uses method channels.
class MethodChannelFancyVideoPlayer extends FancyVideoPlayerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('fancy_video_player');

  @override
  Future<String?> startPlayer(
      {required String url, Map<String, String>? headers}) async {
    final result = await methodChannel
        .invokeMethod<String>('startPlayer', {"url": url, "headers": headers});
    return result;
  }
}
