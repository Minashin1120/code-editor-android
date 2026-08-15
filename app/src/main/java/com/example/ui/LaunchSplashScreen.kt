package com.example.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashDeep = Color(0xFF070B16)
private val SplashMid = Color(0xFF10182C)
private val SplashAccent = Color(0xFF7DD3FC)
private val SplashCyan = Color(0xFF22D3EE)
private val SplashText = Color(0xFFF8FAFC)
private val SplashMuted = Color(0xFF94A3B8)

private data class FloatingToken(
    val text: String,
    val xFraction: Float,
    val yFraction: Float,
    val drift: Float,
    val sizeSp: Float,
    val phase: Float,
)

@Composable
fun LaunchSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val animationsOff = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }
    val logoScale = remember { Animatable(1f) }
    val leftShift = remember { Animatable(0f) }
    val rightShift = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(16f) }
    val taglineAlpha = remember { Animatable(0f) }
    val accentWidth = remember { Animatable(0f) }
    val barProgress = remember { Animatable(0f) }
    var typedLength by remember { mutableIntStateOf(0) }

    val appName = stringResource(R.string.app_name)
    val tagline = stringResource(R.string.splash_tagline)
    val brandDescription = stringResource(R.string.splash_brand_content_description)

    LaunchedEffect(animationsOff) {
        if (animationsOff) {
            onFinished()
            return@LaunchedEffect
        }

        launch {
            logoScale.animateTo(
                1.07f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            )
            logoScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        }
        launch {
            delay(160)
            leftShift.animateTo(-3f, tween(180, easing = FastOutSlowInEasing))
            leftShift.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        launch {
            delay(160)
            rightShift.animateTo(3f, tween(180, easing = FastOutSlowInEasing))
            rightShift.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        launch {
            delay(280)
            titleAlpha.animateTo(1f, tween(420))
            titleOffset.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
        }
        launch {
            delay(320)
            appName.forEachIndexed { index, _ ->
                typedLength = index + 1
                delay(42)
            }
        }
        launch {
            delay(720)
            taglineAlpha.animateTo(1f, tween(400))
            accentWidth.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
        }
        launch {
            delay(180)
            barProgress.animateTo(1f, tween(1700, easing = FastOutSlowInEasing))
        }

        delay(2100)
        launch { exitScale.animateTo(1.06f, tween(380, easing = FastOutSlowInEasing)) }
        exitAlpha.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
        onFinished()
    }

    val infinite = rememberInfiniteTransition(label = "splash-ambient")
    val pulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo-pulse",
    )
    val orbPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing)),
        label = "orb-phase",
    )
    val gridShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "grid-shift",
    )
    val cursorBlink by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursor",
    )

    val tokens = remember {
        listOf(
            FloatingToken("<html>", 0.10f, 0.16f, 14f, 12f, 0.2f),
            FloatingToken("<div>", 0.78f, 0.18f, 18f, 13f, 1.1f),
            FloatingToken("</>", 0.18f, 0.78f, 12f, 14f, 2.4f),
            FloatingToken("<style>", 0.72f, 0.74f, 16f, 12f, 3.2f),
            FloatingToken("<body>", 0.08f, 0.46f, 10f, 11f, 4.0f),
            FloatingToken("<h1>", 0.84f, 0.48f, 15f, 12f, 5.1f),
            FloatingToken("<script>", 0.42f, 0.10f, 11f, 11f, 1.7f),
            FloatingToken("<preview>", 0.58f, 0.86f, 13f, 11f, 2.8f),
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .scale(exitScale.value)
            .alpha(exitAlpha.value)
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashDeep, SplashMid, SplashDeep),
                ),
            )
            .drawBehind {
                val spacing = 28.dp.toPx()
                val shift = gridShift
                for (x in -1..(size.width / spacing).toInt() + 1) {
                    for (y in -1..(size.height / spacing).toInt() + 1) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.035f),
                            radius = 1.4.dp.toPx(),
                            center = Offset(x * spacing + shift * 0.25f, y * spacing - shift * 0.4f),
                        )
                    }
                }

                val orb1 = Offset(
                    size.width * 0.28f + cos(orbPhase.toDouble()).toFloat() * 36.dp.toPx(),
                    size.height * 0.32f + sin(orbPhase.toDouble()).toFloat() * 24.dp.toPx(),
                )
                val orb2 = Offset(
                    size.width * 0.74f + cos(orbPhase.toDouble() + 1.8).toFloat() * 30.dp.toPx(),
                    size.height * 0.62f + sin(orbPhase.toDouble() + 1.2).toFloat() * 34.dp.toPx(),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x66438BFF), Color.Transparent),
                        center = orb1,
                        radius = size.minDimension * 0.42f,
                    ),
                    radius = size.minDimension * 0.42f,
                    center = orb1,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x5522D3EE), Color.Transparent),
                        center = orb2,
                        radius = size.minDimension * 0.36f,
                    ),
                    radius = size.minDimension * 0.36f,
                    center = orb2,
                )
            }
            .semantics { contentDescription = brandDescription },
    ) {
        val density = LocalDensity.current
        val areaWidthPx = with(density) { maxWidth.toPx() }
        val areaHeightPx = with(density) { maxHeight.toPx() }

        tokens.forEach { token ->
            val driftY = sin((orbPhase + token.phase).toDouble()).toFloat() * token.drift
            val driftYPx = with(density) { driftY.dp.toPx() }
            Text(
                text = token.text,
                color = SplashAccent.copy(alpha = 0.16f + 0.08f * pulse),
                fontSize = token.sizeSp.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = (token.xFraction * areaWidthPx).roundToInt(),
                            y = (token.yFraction * areaHeightPx + driftYPx).roundToInt(),
                        )
                    },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SplashLogoMark(
                scale = logoScale.value,
                leftShift = leftShift.value,
                rightShift = rightShift.value,
                glow = pulse,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width((48 * accentWidth.value).dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(listOf(SplashAccent, SplashCyan)),
                    ),
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = titleOffset.value.dp),
            ) {
                Text(
                    text = appName.take(typedLength),
                    color = SplashText.copy(alpha = titleAlpha.value),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .width(2.dp)
                        .height(24.dp)
                        .alpha(
                            if (typedLength == 0) {
                                0f
                            } else if (typedLength < appName.length) {
                                titleAlpha.value
                            } else {
                                cursorBlink * titleAlpha.value
                            },
                        )
                        .background(SplashCyan, RoundedCornerShape(50)),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tagline,
                color = SplashMuted.copy(alpha = taglineAlpha.value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(start = 48.dp, end = 48.dp, bottom = 36.dp)
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barProgress.value.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(SplashAccent, SplashCyan, Color.White)),
                    ),
            )
        }
    }
}

@Composable
private fun SplashLogoMark(
    scale: Float,
    leftShift: Float,
    rightShift: Float,
    glow: Float,
) {
    val density = LocalDensity.current
    val leftPx = with(density) { leftShift.dp.toPx() }
    val rightPx = with(density) { rightShift.dp.toPx() }

    Canvas(
        modifier = Modifier
            .size(112.dp)
            .scale(scale),
    ) {
        val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        val glowRadius = size.minDimension * (0.42f + 0.08f * glow)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0x332563EB), Color(0x2222D3EE)),
            ),
            cornerRadius = CornerRadius(28.dp.toPx()),
            style = Stroke(width = 1.4.dp.toPx()),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SplashAccent.copy(alpha = 0.22f * glow), Color.Transparent),
                center = center,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = center,
        )
        drawRoundRect(
            color = Color(0x2210182C),
            cornerRadius = CornerRadius(28.dp.toPx()),
        )

        val inset = size.width * 0.22f
        val top = size.height * 0.28f
        val bottom = size.height * 0.72f
        val midY = size.height * 0.50f

        drawLine(
            color = SplashAccent,
            start = Offset(size.width * 0.42f + leftPx, top),
            end = Offset(size.width * 0.26f + leftPx, midY),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = SplashAccent,
            start = Offset(size.width * 0.26f + leftPx, midY),
            end = Offset(size.width * 0.42f + leftPx, bottom),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = SplashCyan,
            start = Offset(size.width * 0.58f, size.height * 0.28f),
            end = Offset(size.width * 0.42f, size.height * 0.72f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = SplashAccent,
            start = Offset(size.width * 0.58f + rightPx, top),
            end = Offset(size.width * 0.74f + rightPx, midY),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = SplashAccent,
            start = Offset(size.width * 0.74f + rightPx, midY),
            end = Offset(size.width * 0.58f + rightPx, bottom),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.05f * glow),
            topLeft = Offset(inset * 0.18f, inset * 0.18f),
            size = Size(size.width - inset * 0.36f, size.height - inset * 0.36f),
            cornerRadius = CornerRadius(22.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
