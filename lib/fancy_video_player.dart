import 'fancy_video_player_platform_interface.dart';

class FancyVideoPlayer {
  Future<String?> startPlayer(
      {required String url, Map<String, String>? headers, bool? autoPlay}) {
    return FancyVideoPlayerPlatform.instance
        .startPlayer(url: url, headers: headers, autoPlay: autoPlay);
  }
}
