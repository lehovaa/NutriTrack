package com.nutritrack.app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Decorative fruit/vegetable artwork for the top and bottom of Home.
 *
 * To add real artwork: put the images in res/drawable-nodpi/ as home_produce_top.webp and
 * home_produce_bottom.webp (wide, transparent background), then set the two fields below to
 * R.drawable.home_produce_top / R.drawable.home_produce_bottom. Until then a subtle placeholder
 * of soft shapes is drawn instead.
 */
object HomeArtwork {
    @DrawableRes
    val top: Int? = null

    @DrawableRes
    val bottom: Int? = null
}

enum class DecorationPlacement(val height: Dp) {
    TOP(72.dp),
    BOTTOM(96.dp)
}

/** Purely decorative (no content description); never intercepts touches. */
@Composable
fun ProduceDecoration(
    @DrawableRes image: Int?,
    placement: DecorationPlacement,
    modifier: Modifier = Modifier
) {
    val sized = modifier
        .fillMaxWidth()
        .height(placement.height)
    if (image != null) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = sized
        )
    } else {
        PlaceholderProduceShapes(placement, sized)
    }
}

/** Soft, low-contrast blobs in the theme's produce colors, standing in for real artwork. */
@Composable
private fun PlaceholderProduceShapes(placement: DecorationPlacement, modifier: Modifier) {
    val colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    // (x fraction, y fraction, radius in dp, color index)
    val blobs = when (placement) {
        DecorationPlacement.TOP -> listOf(
            Blob(0.08f, 0.55f, 22f, 0), Blob(0.22f, 0.35f, 14f, 1), Blob(0.40f, 0.62f, 18f, 2),
            Blob(0.62f, 0.38f, 16f, 1), Blob(0.80f, 0.60f, 24f, 0), Blob(0.94f, 0.32f, 12f, 2)
        )
        DecorationPlacement.BOTTOM -> listOf(
            Blob(0.05f, 0.62f, 26f, 1), Blob(0.24f, 0.75f, 18f, 0), Blob(0.45f, 0.55f, 22f, 2),
            Blob(0.66f, 0.78f, 16f, 1), Blob(0.84f, 0.58f, 28f, 0), Blob(0.97f, 0.80f, 14f, 2)
        )
    }
    Canvas(modifier = modifier) {
        blobs.forEach { blob ->
            val radius = blob.radiusDp * density
            drawOval(
                color = colors[blob.colorIndex].copy(alpha = 0.7f),
                topLeft = Offset(size.width * blob.x - radius, size.height * blob.y - radius * 0.85f),
                size = Size(radius * 2, radius * 1.7f)
            )
            // Small "leaf" accent on each blob.
            drawOval(
                color = Color(0xFF7FBF8E).copy(alpha = 0.35f),
                topLeft = Offset(size.width * blob.x - radius * 0.15f, size.height * blob.y - radius * 1.2f),
                size = Size(radius * 0.5f, radius * 0.35f)
            )
        }
    }
}

private data class Blob(val x: Float, val y: Float, val radiusDp: Float, val colorIndex: Int)
