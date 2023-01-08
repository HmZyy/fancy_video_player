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

  final tokyo_renvegers =
      "https://delivery335.akamai-cdn-content.com/hls2/01/10619/xoxkd3009znd_,l,n,h,.urlset/master.m3u8?t=Y_aUAvhSLX4I1btlTNqjfz29BElCv0FcJpqYV-ZtYRw&s=1673190560&e=10800&f=53096030&srv=kvapgsyteecwdmwfggwb&client=46.193.2.72";

  _startPlayer() async {
    var res = await _fancyVideoPlayer.startPlayer(
      url: tokyo_renvegers,
      headers: {
        "Accept-Language": "en-US,en;q=0.6",
        "User-Agent":
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
        "Accept": "*/*",
        "Connection": "keep-alive"
      },
      autoPlay: false,
    );
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
