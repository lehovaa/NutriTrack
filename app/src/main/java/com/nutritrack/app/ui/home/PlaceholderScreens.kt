package com.nutritrack.app.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.ui.theme.NutriTrackTheme

/** Placeholder for the "+" action. Food search will replace the body later. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_add_food)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        PlaceholderMessage(
            icon = R.drawable.ic_add,
            message = R.string.add_food_placeholder,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/** Placeholder History/Calendar destination; shares the bottom bar with Home. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onDiaryClick: () -> Unit,
    onAddFoodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_history)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NutriTrackBottomBar(
                selected = BottomBarDestination.HISTORY,
                onDiaryClick = onDiaryClick,
                onAddClick = onAddFoodClick,
                onHistoryClick = {}
            )
        }
    ) { innerPadding ->
        PlaceholderMessage(
            icon = R.drawable.ic_calendar,
            message = R.string.history_placeholder,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun PlaceholderMessage(
    @DrawableRes icon: Int,
    @StringRes message: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .padding(18.dp)
                    .size(32.dp)
            )
        }
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddFoodScreenPreview() {
    NutriTrackTheme { AddFoodScreen(onClose = {}) }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    NutriTrackTheme { HistoryScreen(onDiaryClick = {}, onAddFoodClick = {}) }
}
