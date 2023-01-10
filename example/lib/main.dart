import 'package:fancy_video_player/fancy_video_player.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

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
      SystemNavigator.pop();
    });

    videoPlayer.startPlayer(
      url:
          "https://wwwx14.gofcdn.com/videos/hls/_GRiofpgzOblIDL8EU-Lfg/1673387737/197443/ea8006e702ad68a125cb3b28a2bfb3fb/ep.1.1673285640.1080.m3u8",
      headers: {
        "Referer":
            "https://gogohd.pro/streaming.php?id=MTk3NDQz&title=Vinland+Saga+Season+2+Episode+1"
      },
      showErrorBox: true,
    );
  }
}
