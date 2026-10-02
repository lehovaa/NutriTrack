package com.nutritrack.app.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.ui.account.AccountAvatar
import com.nutritrack.app.ui.theme.NutriTrackTheme

/**
 * Daily diary (Home): compact header, decorative produce artwork, Today summary, Vitamins and
 * Minerals, Today's meals, more artwork, and the custom bottom bar. Content scrolls normally;
 * the bottom artwork is part of the scrolling content, so it never covers the bar or cards.
 */
@Composable
fun HomeScreen(
    accountPhoto: Uri?,
    onAccountClick: () -> Unit,
    onAddFoodClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: TodaySummary = TodaySummary.Placeholder
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HomeHeader(accountPhoto = accountPhoto, onAccountClick = onAccountClick) },
        bottomBar = {
            NutriTrackBottomBar(
                selected = BottomBarDestination.DIARY,
                onDiaryClick = {},
                onAddClick = onAddFoodClick,
                onHistoryClick = onHistoryClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ProduceDecoration(image = HomeArtwork.top, placement = DecorationPlacement.TOP)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TodaySummaryCard(summary = summary)
                // Equal-height cards side by side; each takes half the width on any screen size.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NutrientCategoryCard(
                        title = R.string.home_vitamins,
                        icon = R.drawable.ic_vitamins,
                        trackedCount = 0,
                        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {}, // Placeholder: will open the 13 vitamins.
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    NutrientCategoryCard(
                        title = R.string.home_minerals,
                        icon = R.drawable.ic_minerals,
                        trackedCount = 0,
                        iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {}, // Placeholder: will open the 15 minerals.
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                MealsCard()
            }
            ProduceDecoration(
                image = HomeArtwork.bottom,
                placement = DecorationPlacement.BOTTOM,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/** "NutriTrack" on the left, the account avatar on the right. No subtitle, by design. */
@Composable
private fun HomeHeader(accountPhoto: Uri?, onAccountClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onAccountClick) {
            AccountAvatar(
                photo = accountPhoto,
                size = 36.dp,
                contentDescription = stringResource(R.string.home_account_button)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NutriTrackTheme {
        HomeScreen(accountPhoto = null, onAccountClick = {}, onAddFoodClick = {}, onHistoryClick = {})
    }
}

@Preview(showBackground = true, widthDp = 320, locale = "ru")
@Composable
private fun HomeScreenNarrowRussianPreview() {
    NutriTrackTheme {
        HomeScreen(accountPhoto = null, onAccountClick = {}, onAddFoodClick = {}, onHistoryClick = {})
    }
}
