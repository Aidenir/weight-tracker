package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Height reserved for the glass bottom nav bar (outer margin + bar + inner spacing). */
val BottomNavHeight: Dp = 92.dp

/** Total clearance a sticky bottom element should keep above the very bottom edge. */
@Composable
fun bottomActionClearance(): Dp {
    val nav = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = nav.calculateBottomPadding()
    return remember(bottomInset) { BottomNavHeight + bottomInset + 8.dp }
}
