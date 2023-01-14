import 'package:fancy_video_player/fancy_video_player.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fancy_video_player/models/Subtitle.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: Example(),
    );
  }
}

class Example extends StatefulWidget {
  const Example({super.key});

  @override
  State<Example> createState() => _ExampleState();
}

class _ExampleState extends State<Example> {
  FancyVideoPlayer videoPlayer = FancyVideoPlayer();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Fancy Video Player Example')),
      body: Center(
        child: OutlinedButton(
          onPressed: () => _startPlayer(),
          child: const Text('Start Player'),
        ),
      ),
    );
  }

  void _startPlayer() {
    List<Subtitle> subtitles = [
      Subtitle(
          "https://cc.zorores.com/5c/c0/5cc0d2efb436af0abd5e985cc554ca0f/rus-10.vtt",
          "russian"),
      Subtitle(
          "https://cc.zorores.com/5c/c0/5cc0d2efb436af0abd5e985cc554ca0f/fre-7.vtt",
          "french"),
    ];

    videoPlayer.setErrorCallback((error) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => Scaffold(
            appBar: AppBar(
              title: Text("backup page"),
            ),
          ),
        ),
      );
    });

    videoPlayer.setOnErrorBoxClicked(() {
      print("Error box clicked");
      // SystemNavigator.pop();

      // Navigator.push(
      //   context,
      //   MaterialPageRoute(
      //     builder: (context) => Scaffold(
      //       appBar: AppBar(
      //         title: Text("another backup page"),
      //       ),
      //     ),
      //   ),
      // );
    });

    videoPlayer.setOnBackPressed(() {
      // SystemNavigator.pop();
    });

    videoPlayer.startPlayer(
      url:
          "https://tc-1.dayimage.net/_v6/4ffd220a1b0c2ac997d68410008fa1bc969fa4150c395ccde923f07e96cd3bdb172bbb0c9f8a913ba948d0bf4600327c27870d16f6de2f37b11dc1257e52eee41d034772aaa591984e07d8663690e9bbe7eac6c0508847b9888a22c113c63e0bee600a780a8468ded132c189007122cd2cb5d888a077b7d127e25433d5ad20a1/master.m3u8",
      headers: {
        "Referer": "https://rapid-cloud.co/",
      },
      subtitles: subtitles,
    );
  }
}
