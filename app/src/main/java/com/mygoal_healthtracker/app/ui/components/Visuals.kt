package com.mygoal_healthtracker.app.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RawRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavyLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    progressColor: Color = MaterialTheme.colorScheme.primary,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "wavyLinear",
    )
    val infinite = rememberInfiniteTransition(label = "wavyLinearMotion")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavyLinearPhase",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(h / 2f, h / 2f),
        )

        val end = (w * animated).coerceIn(0f, w)
        if (end <= 0f) return@Canvas

        val midY = h / 2f
        val amp = (h / 4f).coerceAtMost(6f)
        val wavelength = (w / 4.5f).coerceAtLeast(40f)
        val k = (2f * PI / wavelength).toFloat()
        val path = Path()
        val steps = 64
        for (i in 0..steps) {
            val x = end * (i / steps.toFloat())
            val y = midY + amp * sin(k * x + phase)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(progressColor, progressColor.copy(alpha = 0.85f)),
            ),
            style = Stroke(width = h * 0.55f, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun WavyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 220.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "wavyCircular",
    )
    val infinite = rememberInfiniteTransition(label = "wavyCircularMotion")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavyCircularPhase",
    )
    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val side = size.minDimension
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = side / 2f - 8f
            val strokeWidth = (side * 0.06f).coerceAtMost(18f)

            drawCircle(
                color = trackColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            if (animated <= 0f) return@Canvas

            val path = Path()
            val steps = 240
            val amp = strokeWidth * 0.35f
            val waves = 14f
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                if (t > animated) break
                val angle = (-PI / 2f + 2f * PI * t).toFloat()
                val r = radius + amp * sin(waves * t * (2f * PI).toFloat() + phase)
                val x = centerX + r * cos(angle)
                val y = centerY + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        progressColor.copy(alpha = 0.7f),
                        progressColor,
                        progressColor.copy(alpha = 0.7f),
                    ),
                    center = Offset(centerX, centerY),
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
fun WaveFillBox(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.secondary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    cornerRadius: Dp = 18.dp,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "waveFill",
    )
    val infinite = rememberInfiniteTransition(label = "waveFillMotion")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveFillPhase",
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = cornerRadius.toPx()

        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(r, r),
        )

        if (animated <= 0f) return@Canvas
        val baseline = h * (1f - animated)
        val amp = (h * 0.04f).coerceIn(4f, 12f)
        val wavelength = w / 2f
        val k = (2f * PI / wavelength).toFloat()

        val clipPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, w, h),
                    cornerRadius = CornerRadius(r, r),
                ),
            )
        }
        clipPath(clipPath) {
            val path = Path()
            path.moveTo(0f, baseline + amp * sin(phase))
            val steps = 80
            for (i in 0..steps) {
                val x = (w * i / steps)
                val y = baseline + amp * sin(k * x + phase)
                path.lineTo(x, y)
            }
            path.lineTo(w, h)
            path.lineTo(0f, h)
            path.close()
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor.copy(alpha = 0.95f), fillColor.copy(alpha = 0.75f)),
                ),
            )

            val highlight = Path()
            highlight.moveTo(0f, baseline + amp * sin(phase) - 1f)
            for (i in 0..steps) {
                val x = (w * i / steps)
                val y = baseline + amp * sin(k * x + phase) - 1f
                highlight.lineTo(x, y)
            }
            drawPath(
                path = highlight,
                color = Color.White.copy(alpha = 0.25f),
                style = Stroke(width = 2f, pathEffect = PathEffect.cornerPathEffect(8f)),
            )
        }
    }
}

@Composable
fun VideoBackground(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        runCatching {
                            mediaPlayer.reset()
                            val uri = Uri.parse("android.resource://${ctx.packageName}/$resId")
                            mediaPlayer.setDataSource(ctx, uri)
                            mediaPlayer.setSurface(holder.surface)
                            mediaPlayer.isLooping = true
                            mediaPlayer.setVolume(0f, 0f)
                            mediaPlayer.setOnPreparedListener { it.start() }
                            mediaPlayer.prepareAsync()
                        }
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        runCatching { if (mediaPlayer.isPlaying) mediaPlayer.pause() }
                    }
                })
            }
        },
    )
    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.background,
                ),
            ),
        ),
    ) { content() }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(20.dp),
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
