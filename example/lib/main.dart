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
          "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4s",
      showErrorBox: true,
    );
  }
}
