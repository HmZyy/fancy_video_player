package gg.HmZyy.fancy_video_player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.hardware.SensorManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.util.Rational
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.math.MathUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.Slider
import gg.HmZyy.*
import gg.HmZyy.fancy_video_player.databinding.ActivityPlayerBinding
import gg.HmZyy.fancy_video_player.settings.PlayerSettings
import gg.HmZyy.fancy_video_player.utils.ResettableTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.*
import kotlin.math.roundToInt


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class PlayerActivity : AppCompatActivity(), Player.Listener {
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var videoUri: String
    private lateinit var mediaItem: MediaItem
    private lateinit var cacheFactory: CacheDataSource.Factory
    private lateinit var playbackParameters: PlaybackParameters
    private var orientationListener: OrientationEventListener? = null
    private var headers: Map<String, String> = mapOf()
    var settings = PlayerSettings()


    // States
    private var isInitialized = false
    private var isPlayerPlaying = true
    private var playbackPosition: Long = 0
    private var isBuffering = true
    private var isFullscreen: Int = 0

    // Options
    private var pipEnabled = false
    private var aspectRatio = Rational(16, 9)

    // Views
    private lateinit var exoPlay: ImageButton
    private lateinit var playerView: StyledPlayerView
    private lateinit var exoBrightness: Slider
    private lateinit var exoBrightnessCont: View
    private lateinit var exoVolume: Slider
    private lateinit var exoVolumeCont: View
    private lateinit var exoPip: ImageButton
    private lateinit var exoSkip: View
    private lateinit var exoScreen: ImageButton
    private lateinit var exoSpeed: ImageButton
    private lateinit var exoRotate: ImageButton

    // Handlers
    private val handler = Handler(Looper.getMainLooper())

    //
    private var notchHeight: Int = 0
    var rotation = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initializeNetwork(baseContext)
        videoUri = intent.getStringExtra("url") ?: return
        if (intent.getSerializableExtra("headers") != null){
            val serializableMap = intent.getSerializableExtra("headers") as SerializableMap
            headers = serializableMap.getMap()
        }
        setupPlayer()
        hideSystemBars()
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    private fun initializePlayer(){
        hideSystemBars()

        val httpClient = okHttpClient.newBuilder().apply {
            ignoreAllSSLErrors()
            followRedirects(true)
            followSslRedirects(true)
        }.build()

        val dataSourceFactory = DataSource.Factory {
            val dataSource: HttpDataSource = OkHttpDataSource.Factory(httpClient).createDataSource()
            headers.forEach {
                dataSource.setRequestProperty(it.key, it.value)
            }
            dataSource
        }

        Log.i("flutter", "headers" + headers.toString())
        headers.forEach {
            Log.i("flutter", "key: " + it.key)
            Log.i("flutter", "value: " + it.value)
        }

        val builder = MediaItem.Builder().setUri(videoUri)
        mediaItem = builder.build()

        val trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this)
                    .setDataSourceFactory(dataSourceFactory)
            )
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = true
                this.playbackParameters = this@PlayerActivity.playbackParameters
                setMediaItem(mediaItem)
                prepare()
                seekTo(playbackPosition)
            }

        binding.playerView.player = player

        try {
            val mediaButtonReceiver = ComponentName(this, MediaButtonReceiver::class.java)
            MediaSessionCompat(this, "Player", mediaButtonReceiver, null).let { media ->
                val mediaSessionConnector = MediaSessionConnector(media)
                mediaSessionConnector.setPlayer(player)
                media.isActive = true
            }
        } catch (e: Exception) {
            toast(e.toString())
        }

        player.addListener(this)
        isInitialized = true
    }

    private fun setupPlayer(){
        playerView = findViewById(R.id.player_view)
        exoBrightness = playerView.findViewById(R.id.exo_brightness)
        exoBrightnessCont = playerView.findViewById(R.id.exo_brightness_cont)
        exoPlay = playerView.findViewById(R.id.exo_play)
        exoVolume = playerView.findViewById(R.id.exo_volume)
        exoVolumeCont = playerView.findViewById(R.id.exo_volume_cont)
        exoPip = playerView.findViewById(R.id.exo_pip)
        exoSkip = playerView.findViewById(R.id.exo_skip)
        exoScreen = playerView.findViewById(R.id.exo_screen)
        exoSpeed = playerView.findViewById(R.id.exo_playback_speed)
        exoRotate = playerView.findViewById(R.id.exo_rotate)


        //Play Pause
        exoPlay.setOnClickListener {
            if (isInitialized) {
                isPlayerPlaying = player.isPlaying
                (exoPlay.drawable as Animatable?)?.start()
                if (isPlayerPlaying) {
                    Glide.with(this).load(R.drawable.anim_pause_to_play).into(exoPlay)
                    player.pause()
                } else {
                    Glide.with(this).load(R.drawable.anim_play_to_pause).into(exoPlay)
                    player.play()
                }
            }
        }

        // Picture-in-picture
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pipEnabled = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            if (pipEnabled) {
                exoPip.visibility = View.VISIBLE
                exoPip.setOnClickListener {
                    enterPipMode()
                }
            } else exoPip.visibility = View.GONE
        }

        exoSkip.setOnClickListener {
            if (isInitialized)
                player.seekTo(player.currentPosition + 85 * 1000)
        }

        setupGestures()
    }

    private fun setupGestures () {
        // lock button
        var locked = false
        val container = playerView.findViewById<View>(R.id.exo_controller_cont)
        val screen = playerView.findViewById<View>(R.id.exo_black_screen)
        val lockButton = playerView.findViewById<ImageButton>(R.id.exo_unlock)
        val timeline = playerView.findViewById<ExtendedTimeBar>(R.id.exo_progress)
        playerView.findViewById<ImageButton>(R.id.exo_lock).setOnClickListener {
            locked = true
            screen.visibility = View.GONE
            container.visibility = View.GONE
            lockButton.visibility = View.VISIBLE
            timeline.setForceDisabled(true)
        }
        lockButton.setOnClickListener {
            locked = false
            screen.visibility = View.VISIBLE
            container.visibility = View.VISIBLE
            it.visibility = View.GONE
            timeline.setForceDisabled(false)
        }
        val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        var animationSpeed: Float = 1f
        val gestureSpeed = (300 * animationSpeed).toLong()

        if (Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) != 1) {
            requestedOrientation = rotation
            exoRotate.setOnClickListener {
                requestedOrientation = rotation
                it.visibility = View.GONE
            }
            orientationListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation in 45..135) {
                        if (rotation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) exoRotate.visibility = View.VISIBLE
                        rotation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    } else if (orientation in 225..315) {
                        if (rotation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) exoRotate.visibility = View.VISIBLE
                        rotation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
            }
            orientationListener?.enable()
        }

        // Back button
        playerView.findViewById<ImageButton>(R.id.exo_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val rewindText = playerView.findViewById<TextView>(R.id.exo_fast_rewind_anim)
        val forwardText = playerView.findViewById<TextView>(R.id.exo_fast_forward_anim)
        val fastForwardCard = playerView.findViewById<View>(R.id.exo_fast_forward)
        val fastRewindCard = playerView.findViewById<View>(R.id.exo_fast_rewind)

        //Seeking
        val seekTimerF = ResettableTimer()
        val seekTimerR = ResettableTimer()
        var seekTimesF = 0
        var seekTimesR = 0

        fun seek(forward: Boolean, event: MotionEvent? = null) {
            val views = if (forward) {
                forwardText.text = "+${ settings.seekTime * ++seekTimesF}"
                handler.post { player.seekTo(player.currentPosition + settings.seekTime * 1000) }
                fastForwardCard to forwardText
            } else {
                rewindText.text = "-${settings.seekTime * ++seekTimesR}"
                handler.post { player.seekTo(player.currentPosition - settings.seekTime * 1000) }
                fastRewindCard to rewindText
            }
            startDoubleTapped(views.first, views.second, event, forward)
            if (forward) {
                seekTimerR.reset(object : TimerTask() {
                    override fun run() {
                        stopDoubleTapped(views.first, views.second)
                        seekTimesF = 0
                    }
                }, 850)
            } else {
                seekTimerF.reset(object : TimerTask() {
                    override fun run() {
                        stopDoubleTapped(views.first, views.second)
                        seekTimesR = 0
                    }
                }, 850)
            }
        }

        //Player UI Visibility Handler
        val brightnessRunnable = Runnable {
            if (exoBrightnessCont.alpha == 1f)
                lifecycleScope.launch {
                    ObjectAnimator.ofFloat(exoBrightnessCont, "alpha", 1f, 0f).setDuration(gestureSpeed).start()
                    delay(gestureSpeed)
                    exoBrightnessCont.visibility = View.GONE
                }
        }
        val volumeRunnable = Runnable {
            if (exoVolumeCont.alpha == 1f)
                lifecycleScope.launch {
                    ObjectAnimator.ofFloat(exoVolumeCont, "alpha", 1f, 0f).setDuration(gestureSpeed).start()
                    delay(gestureSpeed)
                    exoVolumeCont.visibility = View.GONE
                }
        }
        playerView.setControllerVisibilityListener(StyledPlayerView.ControllerVisibilityListener { visibility ->
            if (visibility == View.GONE) {
                hideSystemBars()
                brightnessRunnable.run()
                volumeRunnable.run()
            }
        })

        val overshoot = AnimationUtils.loadInterpolator(this, R.anim.over_shoot)
        val controllerDuration = (1f * 200).toLong()
        fun handleController() {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) !isInPictureInPictureMode else true) {
                if (playerView.isControllerFullyVisible) {
                    ObjectAnimator.ofFloat(playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_controller), "alpha", 1f, 0f)
                        .setDuration(controllerDuration).start()
                    ObjectAnimator.ofFloat(playerView.findViewById(R.id.exo_bottom_cont), "translationY", 0f, 128f)
                        .apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(playerView.findViewById(R.id.exo_timeline_cont), "translationY", 0f, 128f)
                        .apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(playerView.findViewById(R.id.exo_top_cont), "translationY", 0f, -128f)
                        .apply { interpolator = overshoot;duration = controllerDuration;start() }
                    playerView.postDelayed({ playerView.hideController() }, controllerDuration)
                } else {
                    playerView.showController()
                    ObjectAnimator.ofFloat(playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_controller), "alpha", 0f, 1f)
                        .setDuration(controllerDuration).start()
                    ObjectAnimator.ofFloat(playerView.findViewById(R.id.exo_bottom_cont), "translationY", 128f, 0f)
                        .apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(playerView.findViewById(R.id.exo_timeline_cont), "translationY", 128f, 0f)
                        .apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(playerView.findViewById(R.id.exo_top_cont), "translationY", -128f, 0f)
                        .apply { interpolator = overshoot;duration = controllerDuration;start() }
                }
            }
        }

        playerView.findViewById<View>(R.id.exo_full_area).setOnClickListener {
            handleController()
        }

        fun doubleTap(forward: Boolean, event: MotionEvent) {
            if (!locked && isInitialized) {
                seek(forward, event)
            }
        }

        //Brightness
        var brightnessTimer = Timer()
        exoBrightnessCont.visibility = View.GONE

        fun brightnessHide() {
            brightnessTimer.cancel()
            brightnessTimer.purge()
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    handler.post(brightnessRunnable)
                }
            }
            brightnessTimer = Timer()
            brightnessTimer.schedule(timerTask, 3000)
        }
        exoBrightness.value = (getCurrentBrightnessValue(this) * 10f)

        exoBrightness.addOnChangeListener { _, value, _ ->
            val lp = window.attributes
            lp.screenBrightness = brightnessConverter(value / 10, false)
            window.attributes = lp
            brightnessHide()
        }

        //Volume
        var volumeTimer = Timer()
        exoVolumeCont.visibility = View.GONE

        val volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        exoVolume.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / volumeMax * 10
        fun volumeHide() {
            volumeTimer.cancel()
            volumeTimer.purge()
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    handler.post(volumeRunnable)
                }
            }
            volumeTimer = Timer()
            volumeTimer.schedule(timerTask, 3000)
        }
        exoVolume.addOnChangeListener { _, value, _ ->
            val volume = (value / 10 * volumeMax).roundToInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            volumeHide()
        }

        //FastRewind (Left Panel)
        val fastRewindDetector = GestureDetector(this, object : GesturesListener() {
            override fun onDoubleClick(event: MotionEvent) {
                doubleTap(false, event)
            }

            override fun onScrollYClick(y: Float) {
                if (!locked) {
                    exoBrightness.value =
                        MathUtils.clamp(exoBrightness.value + y / 100, 0f, 10f)
                    if (exoBrightnessCont.visibility != View.VISIBLE) {
                        exoBrightnessCont.visibility = View.VISIBLE
                    }
                    exoBrightnessCont.alpha = 1f
                }
            }

            override fun onSingleClick(event: MotionEvent) = handleController()
        })
        val rewindArea = playerView.findViewById<View>(R.id.exo_rewind_area)
        rewindArea.isClickable = true
        rewindArea.setOnTouchListener { v, event ->
            fastRewindDetector.onTouchEvent(event)
            v.performClick()
            true
        }

        //FastForward (Right Panel)
        val fastForwardDetector = GestureDetector(this, object : GesturesListener() {
            override fun onDoubleClick(event: MotionEvent) {
                doubleTap(true, event)
            }

            override fun onScrollYClick(y: Float) {
                if (!locked) {
                    exoVolume.value = MathUtils.clamp(exoVolume.value + y / 100, 0f, 10f)
                    if (exoVolumeCont.visibility != View.VISIBLE) {
                        exoVolumeCont.visibility = View.VISIBLE
                    }
                    exoVolumeCont.alpha = 1f
                }
            }

            override fun onSingleClick(event: MotionEvent) = handleController()
        })
        val forwardArea = playerView.findViewById<View>(R.id.exo_forward_area)
        forwardArea.isClickable = true
        forwardArea.setOnTouchListener { v, event ->
            fastForwardDetector.onTouchEvent(event)
            v.performClick()
            true
        }

        // Fullscreen
        playerView.resizeMode = when (isFullscreen) {
            0    -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            1    -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            2    -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }

        exoScreen.setOnClickListener {
            if (isFullscreen < 2) isFullscreen += 1 else isFullscreen = 0
            playerView.resizeMode = when (isFullscreen) {
                0    -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                1    -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                2    -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            toastString(
                when (isFullscreen) {
                    0    -> "Original"
                    1    -> "Zoom"
                    2    -> "Stretch"
                    else -> "Original"
                }
            )
        }


        //Cast
        playerView.findViewById<ImageButton>(R.id.exo_cast).apply {
//            visibility = View.VISIBLE
            setSafeOnClickListener {
                cast()
            }
        }

        // Playback Speed
        val speeds =
                arrayOf(0.25f, 0.33f, 0.5f, 0.66f, 0.75f, 1f, 1.25f, 1.33f, 1.5f, 1.66f, 1.75f, 2f)

        val speedsName = speeds.map { "${it}x" }.toTypedArray()
        var curSpeed = settings.defaultSpeed

        playbackParameters = PlaybackParameters(speeds[curSpeed])
        var speed: Float
        val speedDialog = AlertDialog.Builder(this, R.style.DialogTheme).setTitle("Speed")
        exoSpeed.setOnClickListener {
            speedDialog.setSingleChoiceItems(speedsName, curSpeed) { dialog, i ->
                if (isInitialized) {
                    speed = speeds[i]
                    curSpeed = i
                    playbackParameters = PlaybackParameters(speed)
                    player.playbackParameters = playbackParameters
                    dialog.dismiss()
                    hideSystemBars()
                }
            }.show()
        }
        speedDialog.setOnCancelListener { hideSystemBars() }
    }

    private fun releasePlayer(){
        isPlayerPlaying = player.playWhenReady
        playbackPosition = player.currentPosition
        player.release()
        VideoCache.release()
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onRenderedFirstFrame() {
        super.onRenderedFirstFrame()
        val height = (player.videoFormat ?: return).height
        val width = (player.videoFormat ?: return).width
        aspectRatio = Rational(width, height)
        if (player.duration < playbackPosition)
            player.seekTo(0)
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == ExoPlayer.STATE_READY) {
            player.play()
        }
        isBuffering = playbackState == Player.STATE_BUFFERING
        super.onPlaybackStateChanged(playbackState)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!isBuffering) {
            isPlayerPlaying = isPlaying
            playerView.keepScreenOn = isPlaying
            (exoPlay.drawable as Animatable?)?.start()
            if (!this.isDestroyed) Glide.with(this)
                .load(if (isPlaying) R.drawable.anim_play_to_pause else R.drawable.anim_pause_to_play).into(exoPlay)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
//        onBackPressedDispatcher.onBackPressed()
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
            -> {
                showToast("Source Exception : ${error.message}")
                isPlayerPlaying = true
            }
            else
            -> showToast("Player Error ${error.errorCode} (${error.errorCodeName}) : ${error.message}")
        }
    }

    //Double Tap Animation
    private fun startDoubleTapped(v: View, text: TextView, event: MotionEvent? = null, forward: Boolean) {
        ObjectAnimator.ofFloat(text, "alpha", 1f, 1f).setDuration(600).start()
        ObjectAnimator.ofFloat(text, "alpha", 0f, 1f).setDuration(150).start()

        (text.compoundDrawables[1] as Animatable).apply {
            if (!isRunning) start()
        }

        if (event != null) {
            playerView.hideController()
            v.circularReveal(event.x.toInt(), event.y.toInt(), !forward, 800)
            ObjectAnimator.ofFloat(v, "alpha", 1f, 1f).setDuration(800).start()
            ObjectAnimator.ofFloat(v, "alpha", 0f, 1f).setDuration(300).start()
        }
    }

    private fun stopDoubleTapped(v: View, text: TextView) {
        handler.post {
            ObjectAnimator.ofFloat(v, "alpha", v.alpha, 0f).setDuration(150).start()
            ObjectAnimator.ofFloat(text, "alpha", 1f, 0f).setDuration(150).start()
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }


    // Cast
    private fun cast() {
        val videoURL = videoUri ?: return
        val shareVideo = Intent(Intent.ACTION_VIEW)
        shareVideo.setDataAndType(Uri.parse(videoURL), "video/*")
        shareVideo.setPackage("com.instantbits.cast.webvideo")
//        shareVideo.putExtra("title", media.userPreferredName + " : Ep " + episodeTitleArr[currentEpisodeIndex])
        val headers = Bundle()
//        video?.url?.headers?.forEach {
//            headers.putString(it.key, it.value)
//        }
        shareVideo.putExtra("android.media.intent.extra.HTTP_HEADERS", headers)
        shareVideo.putExtra("secure_uri", true)
        try {
            startActivity(shareVideo)
        } catch (ex: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uriString = "market://details?id=com.instantbits.cast.webvideo"
            intent.data = Uri.parse(uriString)
            startActivity(intent)
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun enterPipMode() {
        if (!pipEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(
                    PictureInPictureParams
                        .Builder()
                        .setAspectRatio(aspectRatio)
                        .build()
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                enterPictureInPictureMode()
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if (isInitialized) {
            releasePlayer()
        }

        super.onDestroy()
        finishAndRemoveTask()
    }

}