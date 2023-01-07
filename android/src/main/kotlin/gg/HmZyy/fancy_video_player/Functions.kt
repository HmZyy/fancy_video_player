package gg.HmZyy
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.*
import android.provider.Settings
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.core.math.MathUtils
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.Serializable
import java.lang.reflect.Field
import java.util.*
import kotlin.math.log2
import kotlin.math.pow

val defaultHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
)

class SerializableMap(private val map: Map<String, String>) : Serializable {
    fun getMap(): Map<String, String> {
        return map
    }
}

@SuppressLint("ClickableViewAccessibility")
class SpinnerNoSwipe : androidx.appcompat.widget.AppCompatSpinner {
    private var mGestureDetector: GestureDetector? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return performClick()
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector!!.onTouchEvent(event)
        return true
    }
}

@SuppressLint("ViewConstructor")
class ExtendedTimeBar(
    context: Context,
    attrs: AttributeSet?
) : DefaultTimeBar(context, attrs) {
    private var enabled = false
    private var forceDisabled = false
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        super.setEnabled(!forceDisabled && this.enabled)
    }

    fun setForceDisabled(forceDisabled: Boolean) {
        this.forceDisabled = forceDisabled
        isEnabled = enabled
    }
}

abstract class GesturesListener : GestureDetector.SimpleOnGestureListener() {
    private var timer: Timer? = null //at class level;
    private val delay: Long = 200

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        processSingleClickEvent(e)
        return super.onSingleTapUp(e)
    }

    override fun onLongPress(e: MotionEvent) {
        processLongClickEvent(e)
        super.onLongPress(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        processDoubleClickEvent(e)
        return super.onDoubleTap(e)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        onScrollYClick(distanceY)
        onScrollXClick(distanceX)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    private fun processSingleClickEvent(e: MotionEvent) {
        val handler = Handler(Looper.getMainLooper())
        val mRunnable = Runnable {
            onSingleClick(e)
        }
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handler.post(mRunnable)
                }
            }, delay)
        }
    }

    private fun processDoubleClickEvent(e: MotionEvent) {
        timer?.apply {
            cancel()
            purge()
        }
        onDoubleClick(e)
    }

    private fun processLongClickEvent(e: MotionEvent) {
        timer?.apply {
            cancel()
            purge()
        }
        onLongClick(e)
    }

    open fun onSingleClick(event: MotionEvent) {}
    open fun onDoubleClick(event: MotionEvent) {}
    open fun onScrollYClick(y: Float) {}
    open fun onScrollXClick(y: Float) {}
    open fun onLongClick(event: MotionEvent) {}
}

@Suppress("DEPRECATION")
fun Activity.hideSystemBars() {
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
}
fun getCurrentBrightnessValue(context: Context): Float {
    fun getMax(): Int {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val fields: Array<Field> = powerManager.javaClass.declaredFields
        for (field in fields) {
            if (field.name.equals("BRIGHTNESS_ON")) {
                field.isAccessible = true
                return try {
                    field.get(powerManager)?.toString()?.toInt() ?: 255
                } catch (e: IllegalAccessException) {
                    255
                }
            }
        }
        return 255
    }

    fun getCur(): Float {
        return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 127).toFloat()
    }

    return brightnessConverter(getCur() / getMax(), true)
}

fun brightnessConverter(it: Float, fromLog: Boolean) =
    MathUtils.clamp(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            if (fromLog) log2((it * 256f)) * 12.5f / 100f else 2f.pow(it * 100f / 12.5f) / 256f
        else it, 0.001f, 1f
    )

fun logError(e: Exception) {
    toastString(e.localizedMessage)
    e.printStackTrace()
}

fun toastString(s: String?, activity: Activity? = null) {
}

class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
object VideoCache {
    private var simpleCache: SimpleCache? = null
    fun getInstance(context: Context): SimpleCache {
        val databaseProvider = StandaloneDatabaseProvider(context)
        if (simpleCache == null)
            simpleCache = SimpleCache(
                File(context.cacheDir, "exoplayer").also { it.deleteOnExit() }, // Ensures always fresh file
                LeastRecentlyUsedCacheEvictor(300L * 1024L * 1024L),
                databaseProvider
            )
        return simpleCache as SimpleCache
    }

    fun release() {
        simpleCache?.release()
        simpleCache = null
    }
}
