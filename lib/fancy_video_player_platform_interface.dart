import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'fancy_video_player_method_channel.dart';
import 'package:fancy_video_player/models/Subtitle.dart';

abstract class FancyVideoPlayerPlatform extends PlatformInterface {
  /// Constructs a FancyVideoPlayerPlatform.
  FancyVideoPlayerPlatform() : super(token: _token);

  static final Object _token = Object();

  static FancyVideoPlayerPlatform _instance = MethodChannelFancyVideoPlayer();

  /// The default instance of [FancyVideoPlayerPlatform] to use.
  ///
  /// Defaults to [MethodChannelFancyVideoPlayer].
  static FancyVideoPlayerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FancyVideoPlayerPlatform] when
  /// they register themselves.
  static set instance(FancyVideoPlayerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> startPlayer({
    required String url,
    Map<String, String>? headers,
    bool autoPlay = true,
    bool closeOnError = false,
    bool showErrorBox = false,
    List<Subtitle>? subtitles,
  }) {
    throw UnimplementedError('startPlayer() has not been implemented.');
  }

  void setErrorCallback(Function callback) {
    throw UnimplementedError('setErrorCallback() has not been implemented.');
  }

  void setOnErrorBoxClicked(Function callback) {
    throw UnimplementedError(
        'setOnErrorBoxClicked() has not been implemented.');
  }

  void setOnBackPressed(Function callback) {
    throw UnimplementedError('setOnBackPressed() has not been implemented.');
  }

  void setOnEnterPictureInPicture(Function callback) {
    throw UnimplementedError(
        'setOnEnterPictureInPicture() has not been implemented.');
  }
}
