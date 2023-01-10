package gg.HmZyy.fancy_video_player

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.exoplayer2.PlaybackException
import gg.HmZyy.SerializableMap

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FancyVideoPlayerPlugin */
class FancyVideoPlayerPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fancy_video_player")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "startPlayer") {
      val url = call.argument<String>("url")
      var headers = call.argument<Map<String, String>>("headers")
      var autoPlay = call.argument<Boolean>("autoPlay")
      var closeOnError = call.argument<Boolean>("closeOnError")
      var showErrorBox = call.argument<Boolean>("showErrorBox")
      if (headers == null) {
        headers = emptyMap()
        Log.e("flutter", "headers is null")
      }
      Log.i("flutter", url ?: "no url")
      if (url != null) {
        startPlayer(url, headers, autoPlay, closeOnError, showErrorBox)
        result.success("Launched Success")
      }
      result.error("No Url provided","Failed to launch", "tried to call startPlayer with no url")
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun startPlayer(url: String, headers: Map<String, String>, autoPlay: Boolean?, closeOnError: Boolean?, showErrorBox: Boolean?) {
    val serializableHeaders = SerializableMap(headers)
    val intent = Intent(context, PlayerActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra("url", url)
    intent.putExtra("headers", serializableHeaders)
    intent.putExtra("autoPlay", autoPlay)
    intent.putExtra("closeOnError", closeOnError)
    intent.putExtra("showErrorBox", showErrorBox)
    context.startActivity(intent)
  }

  companion object {
    private lateinit var channel : MethodChannel
    fun onPlayerError(error: PlaybackException) {
      Log.i("flutter", "calling onPlayerError from FancyVideoPlayerPlugin")
      channel.invokeMethod("onPlayerError", error.errorCode)
    }

    fun onErrorBoxClicked() {
      Log.i("flutter", "calling onErrorBoxClicked from FancyVideoPlayerPlugin")
      channel.invokeMethod("onErrorBoxClicked", true)
    }

    fun onBackPressed() {
      Log.i("flutter", "calling onBackPressed from FancyVideoPlayerPlugin")
      channel.invokeMethod("onBackPressed", true)
    }
  }
}
