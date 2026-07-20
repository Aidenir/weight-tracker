package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Height reserved for the glass bottom nav bar (outer margin + bar + inner spacing). */
val BottomNavHeight: Dp = 92.dp

/**
 * Total clearance a sticky bottom element should keep above the very bottom edge.
 *
 * When the keyboard is up we assume the screen has already `imePadding`'d its
 * container, so the sticky element is being measured against a box that ends
 * just above the keyboard. In that case we only want a small breathing gap —
 * reserving room for the tab bar makes no sense when the tab bar is hidden
 * behind the keyboard anyway.
 */
@Composable
fun bottomActionClearance(): Dp {
    val nav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val ime = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    return if (ime > 0.dp) 8.dp else BottomNavHeight + nav + 8.dp
}
