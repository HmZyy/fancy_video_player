import 'fancy_video_player_platform_interface.dart';

class FancyVideoPlayer {
  Future<String?> startPlayer({
    required String url,
    Map<String, String>? headers,
    bool? autoPlay,
    bool closeOnError = false,
  }) {
    return FancyVideoPlayerPlatform.instance.startPlayer(
      url: url,
      headers: headers,
      autoPlay: autoPlay,
      closeOnError: closeOnError,
    );
  }

  void setErrorCallback(Function callback) {
    FancyVideoPlayerPlatform.instance.setErrorCallback(callback);
  }
}
