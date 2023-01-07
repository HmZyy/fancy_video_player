import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:fancy_video_player/fancy_video_player.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _fancyVideoPlayer = FancyVideoPlayer();
  final _url =
      "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4";

  _startPlayer() async {
    var res = await _fancyVideoPlayer.startPlayer(url: _url);
    print(res);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('fancy video player example app'),
        ),
        body: Center(
            child: ElevatedButton(
          onPressed: _startPlayer,
          child: const Text("start player"),
        )),
      ),
    );
  }
}
