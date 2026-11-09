package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DialPosition
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.RamsAmberAttack
import com.example.ui.theme.RamsAmberContainer
import com.example.ui.theme.RamsBackground
import com.example.ui.theme.RamsOutline
import com.example.ui.theme.RamsSageContainer
import com.example.ui.theme.RamsSageRecovery
import com.example.ui.theme.RamsSurface
import com.example.ui.theme.RamsTextMuted
import com.example.ui.theme.RamsTextPrimary
import com.example.ui.theme.RamsTextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dieter Rams / Braun inspired rotary dial gauge for the Ovicx Q200-O mechanical resistance knob.
 * 12 o'clock (0°) is "РП" (Рабочая Полка).
 * -45° is 10:30, +45° is 1:30, +90° is 3:00, +135° is 4:30, +180° is 6:00.
 */
@Composable
fun DialGaugeWidget(
    currentDial: DialPosition,
    previousDial: DialPosition?,
    deltaInstruction: String,
    isAttackPhase: Boolean,
    highlightDelta: Boolean,
    modifier: Modifier = Modifier,
    dialSize: Dp = 156.dp
) {
    val animatedAngle by animateFloatAsState(
        targetValue = currentDial.angleDegrees,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dial_angle"
    )

    val accentColor = if (isAttackPhase) RamsAmberAttack else RamsSageRecovery
    val deltaBgColor = if (highlightDelta) {
        if (isAttackPhase) RamsAmberContainer else RamsSageContainer
    } else {
        RamsSurface
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dial_gauge_widget"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Delta Turn Banner (prominently highlighted on phase change)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = deltaBgColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = deltaInstruction,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.8.sp
                ),
                color = if (highlightDelta) accentColor else RamsTextPrimary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Precision Canvas Rotary Knob
            Box(
                modifier = Modifier.size(dialSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(dialSize)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f - 8.dp.toPx()
                    val knobRadius = outerRadius * 0.66f

                    // Outer instrument ring
                    drawCircle(
                        color = RamsSurface,
                        radius = outerRadius,
                        center = center
                    )
                    drawCircle(
                        color = RamsOutline,
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw delta sweep arc from previous position to current position
                    val prevAngle = previousDial?.angleDegrees ?: 0f
                    val sweep = animatedAngle - prevAngle
                    if (kotlin.math.abs(sweep) > 1f) {
                        val arcRadius = outerRadius - 6.dp.toPx()
                        drawArc(
                            color = accentColor.copy(alpha = 0.45f),
                            startAngle = prevAngle - 90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                            size = Size(arcRadius * 2f, arcRadius * 2f),
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw minor tick marks around the dial (-90° to +180°)
                    for (deg in -90..180 step 15) {
                        val rad = (deg - 90) * PI / 180.0
                        val isMajor = deg % 45 == 0
                        val tickStart = if (isMajor) outerRadius - 14.dp.toPx() else outerRadius - 8.dp.toPx()
                        val tickEnd = outerRadius - 3.dp.toPx()
                        val startOffset = Offset(
                            x = center.x + (tickStart * cos(rad)).toFloat(),
                            y = center.y + (tickStart * sin(rad)).toFloat()
                        )
                        val endOffset = Offset(
                            x = center.x + (tickEnd * cos(rad)).toFloat(),
                            y = center.y + (tickEnd * sin(rad)).toFloat()
                        )
                        val tickColor = when {
                            deg == currentDial.angleDegrees.toInt() -> accentColor
                            deg == 0 -> RamsSageRecovery
                            isMajor -> RamsTextPrimary.copy(alpha = 0.75f)
                            else -> RamsTextMuted.copy(alpha = 0.4f)
                        }
                        drawLine(
                            color = tickColor,
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Special 12:00 "РП" reference marker outside top edge
                    drawCircle(
                        color = RamsSageRecovery,
                        radius = 3.5.dp.toPx(),
                        center = Offset(center.x, center.y - outerRadius + 2.dp.toPx())
                    )

                    // Inner mechanical barrel
                    drawCircle(
                        color = RamsBackground,
                        radius = knobRadius,
                        center = center
                    )
                    drawCircle(
                        color = RamsOutline,
                        radius = knobRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Pointer needle from center to knob edge
                    val pointerRad = (animatedAngle - 90f) * PI / 180.0
                    val needleTip = Offset(
                        x = center.x + ((knobRadius - 6.dp.toPx()) * cos(pointerRad)).toFloat(),
                        y = center.y + ((knobRadius - 6.dp.toPx()) * sin(pointerRad)).toFloat()
                    )
                    val needleTail = Offset(
                        x = center.x - ((knobRadius * 0.22f) * cos(pointerRad)).toFloat(),
                        y = center.y - ((knobRadius * 0.22f) * sin(pointerRad)).toFloat()
                    )
                    drawLine(
                        color = accentColor,
                        start = needleTail,
                        end = needleTip,
                        strokeWidth = 4.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Center pivot cap
                    drawCircle(
                        color = RamsTextPrimary,
                        radius = 5.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = RamsBackground,
                        radius = 2.dp.toPx(),
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Right-side Instrument Readout for the Knob
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ПОЛОЖЕНИЕ РЫЧАЖКА",
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsTextSecondary
                )
                Text(
                    text = currentDial.shortLabel,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    ),
                    color = accentColor
                )
                Text(
                    text = "Метка на ${currentDial.clockLabel}",
                    style = MaterialTheme.typography.labelLarge,
                    color = RamsTextPrimary
                )
                Text(
                    text = currentDial.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RamsTextSecondary
                )
                AnimatedVisibility(
                    visible = previousDial != null && previousDial != currentDial,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Было: ${previousDial?.shortLabel ?: "РП"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = RamsTextMuted
                    )
                }
            }
        }
    }
}
