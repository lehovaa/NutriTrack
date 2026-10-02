package com.nutritrack.app.ui.account

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.nutritrack.app.R

/**
 * Circular account avatar: the profile photo when one is set, otherwise a generic person icon.
 * Decorative by default; callers describe the surrounding control for accessibility.
 */
@Composable
fun AccountAvatar(
    photo: Uri?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(size)
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = contentDescription,
                modifier = Modifier.padding(size / 5)
            )
        }
    }
}
