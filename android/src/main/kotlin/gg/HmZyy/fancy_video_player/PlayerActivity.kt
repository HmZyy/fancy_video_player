package gg.HmZyy.fancy_video_player

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.math.MathUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.Slider
import gg.HmZyy.GesturesListener
import gg.HmZyy.brightnessConverter
import gg.HmZyy.fancy_video_player.databinding.ActivityPlayerBinding
import gg.HmZyy.getCurrentBrightnessValue
import gg.HmZyy.hideSystemBars
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var videoUri: String

    // States
    private var isInitialized = false
    private var isPlayerPlaying = true

    // Options
    private lateinit var exoPlay: ImageButton
    private lateinit var playerView: StyledPlayerView
    private lateinit var exoBrightness: Slider
    private lateinit var exoBrightnessCont: View
    private lateinit var exoVolume: Slider
    private lateinit var exoVolumeCont: View

    // Handlers
    private val handler = Handler(Looper.getMainLooper())

    //
    private var notchHeight: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        videoUri = intent.getStringExtra("url") ?: return
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
        val trackSelector = DefaultTrackSelector(this)
        val loadControl = DefaultLoadControl()
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        val mediaItem: MediaItem = MediaItem.fromUri(videoUri)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()
        isInitialized = true
    }

    private fun setupPlayer(){
        playerView = findViewById(R.id.player_view)
        exoBrightness = playerView.findViewById(R.id.exo_brightness)
        exoBrightnessCont = playerView.findViewById(R.id.exo_brightness_cont)
        exoPlay = playerView.findViewById(R.id.exo_play)
        exoVolume = playerView.findViewById(R.id.exo_volume)
        exoVolumeCont = playerView.findViewById(R.id.exo_volume_cont)


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
        setupGestures()
    }

    private fun setupGestures () {
        var locked = false

        val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        var animationSpeed: Float = 1f
        val gestureSpeed = (300 * animationSpeed).toLong()


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

        fun doubleTap(forward: Boolean, event: MotionEvent) {
//            if (!locked && isInitialized && settings.doubleTap) {
//                seek(forward, event)
//            }
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

//            override fun onSingleClick(event: MotionEvent) = handleController()
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

//            override fun onSingleClick(event: MotionEvent) = handleController()
        })
        val forwardArea = playerView.findViewById<View>(R.id.exo_forward_area)
        forwardArea.isClickable = true
        forwardArea.setOnTouchListener { v, event ->
            fastForwardDetector.onTouchEvent(event)
            v.performClick()
            true
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            player.release()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            player.release()
        }
    }
}