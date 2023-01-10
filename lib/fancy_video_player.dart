import 'fancy_video_player_platform_interface.dart';

class FancyVideoPlayer {
  Future<String?> startPlayer({
    required String url,
    Map<String, String>? headers,
    bool? autoPlay,
    bool closeOnError = false,
    bool showErrorBox = false,
  }) {
    return FancyVideoPlayerPlatform.instance.startPlayer(
      url: url,
      headers: headers,
      autoPlay: autoPlay,
      closeOnError: closeOnError,
      showErrorBox: showErrorBox,
    );
  }

  void setErrorCallback(Function callback) {
    FancyVideoPlayerPlatform.instance.setErrorCallback(callback);
  }

  void setOnErrorBoxClicked(Function callback) {
    FancyVideoPlayerPlatform.instance.setOnErrorBoxClicked(callback);
  }

  void setOnBackPressed(Function callback) {
    FancyVideoPlayerPlatform.instance.setOnBackPressed(callback);
  }

  void setOnEnterPictureInPicture(Function callback) {
    FancyVideoPlayerPlatform.instance.setOnEnterPictureInPicture(callback);
  }
}
