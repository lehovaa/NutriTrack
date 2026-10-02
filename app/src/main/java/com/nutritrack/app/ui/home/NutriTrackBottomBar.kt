package com.nutritrack.app.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R

enum class BottomBarDestination { DIARY, HISTORY }

private val BarHeight = 68.dp
private val AddButtonSize = 64.dp
private val AddButtonOverhang = 16.dp

/**
 * Diary · large "+" · History. The "+" rises above the bar so it stands out. The bar pads itself
 * for the system navigation bar, so it never sits under gesture or button navigation.
 */
@Composable
fun NutriTrackBottomBar(
    selected: BottomBarDestination,
    onDiaryClick: () -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AddButtonOverhang)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(BarHeight)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarItem(
                    icon = R.drawable.ic_diary,
                    label = R.string.nav_diary,
                    selected = selected == BottomBarDestination.DIARY,
                    onClick = onDiaryClick
                )
                Spacer(modifier = Modifier.size(AddButtonSize))
                BarItem(
                    icon = R.drawable.ic_calendar,
                    label = R.string.nav_history,
                    selected = selected == BottomBarDestination.HISTORY,
                    onClick = onHistoryClick
                )
            }
        }
        FloatingActionButton(
            onClick = onAddClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(AddButtonSize)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.nav_add_food),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun BarItem(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .widthIn(min = 72.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color)
        Text(text = stringResource(label), style = MaterialTheme.typography.labelMedium, color = color)
    }
}
