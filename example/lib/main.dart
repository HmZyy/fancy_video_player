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
      "https://delivery207.akamai-cdn-content.com/hls2/01/10618/ipwisjg0vmlm_,l,n,h,.urlset/master.m3u8?t=CGj8hxU3LkuA7Wt8-t7_-8GOzOO4NL55QUAZGUpDAeA&s=1673115974&e=10800&f=53093095&srv=sto221&client=46.193.2.72";
  final _headers = {
    'Accept-Language': 'en-US,en;q=0.6',
    'User-Agent':
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36',
    'Accept': '*/*',
    'Accept-Encoding': 'gzip, deflate, br',
    'Connection': 'keep-alive',
  };

  _startPlayer() async {
    var res = await _fancyVideoPlayer.startPlayer(url: _url, headers: _headers);
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
