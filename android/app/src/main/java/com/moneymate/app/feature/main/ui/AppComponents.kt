package com.moneymate.app.feature.main.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: AppComponents.kt
// Purpose: Reusable MoneyMate UI building blocks used throughout the authenticated app.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.ui.theme.LocalMoneyMateTokens

/**
 * Shared native-Compose components that mirror the structure of
 * reference/moneymate-frontend.html while using only the PDF color system.
 */

// -----------------------------------------------------------------------------
// Section: HtmlIconButton
// Purpose: Encapsulates the Html Icon Button section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    ghost: Boolean = false,
    tint: Color? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalMoneyMateTokens.current
    Surface(
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (ghost) Color.Transparent else c.surface,
        border = if (ghost) null else BorderStroke(1.dp, c.divider),
        shadowElevation = if (ghost) 0.dp else 1.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint ?: c.primaryText,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlTopBar
// Purpose: Encapsulates the Html Top Bar section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlTopBar(
    title: String? = null,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    rightContent: @Composable RowScope.() -> Unit = {}
) {
    val c = LocalMoneyMateTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            HtmlIconButton(Icons.Filled.ArrowBack, "Back", onBack, ghost = true)
            Spacer(Modifier.width(4.dp))
        }
        if (title != null) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = c.primaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.1).sp
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, color = c.secondaryText, fontSize = 11.5.sp)
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        rightContent()
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlCard
// Purpose: Encapsulates the Html Card section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalMoneyMateTokens.current
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.divider),
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(padding), content = content)
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.divider),
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(padding), content = content)
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlSectionHeader
// Purpose: Encapsulates the Html Section Header section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlSectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalMoneyMateTokens.current
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = c.primaryText,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(action, color = c.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.ChevronRight, null, tint = c.action, modifier = Modifier.size(16.dp))
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlEyebrow
// Purpose: Encapsulates the Html Eyebrow section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlEyebrow(text: String, modifier: Modifier = Modifier) {
    val c = LocalMoneyMateTokens.current
    Text(
        text.uppercase(),
        color = c.mutedText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = modifier
    )
}


// -----------------------------------------------------------------------------
// Section: HtmlChip
// Purpose: Encapsulates the Html Chip section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalMoneyMateTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) c.action else c.surface,
        border = BorderStroke(1.dp, if (selected) c.action else c.border),
        onClick = onClick
    ) {
        Text(
            text,
            color = if (selected) Color.White else c.secondaryText,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
        )
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlQuickAction
// Purpose: Encapsulates the Html Quick Action section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalMoneyMateTokens.current
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(17.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.divider),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = c.action, modifier = Modifier.size(23.dp))
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(label, color = c.secondaryText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlSettingsRow
// Purpose: Encapsulates the Html Settings Row section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlSettingsRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    destructive: Boolean = false,
    trailingText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val c = LocalMoneyMateTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 6.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(if (destructive) c.lightError else c.background, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (destructive) c.error else c.secondaryText,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (destructive) c.error else c.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = c.secondaryText,
                    fontSize = 11.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!trailingText.isNullOrBlank()) {
            Text(trailingText, color = c.secondaryText, fontSize = 11.5.sp)
            Spacer(Modifier.width(5.dp))
        }
        if (trailing != null) trailing()
        else if (onClick != null) Icon(Icons.Filled.ChevronRight, null, tint = c.mutedText, modifier = Modifier.size(19.dp))
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlDivider
// Purpose: Encapsulates the Html Divider section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HtmlDivider() {
    val c = LocalMoneyMateTokens.current
    HorizontalDivider(color = c.divider, thickness = 1.dp, modifier = Modifier.padding(horizontal = 6.dp))
}


// -----------------------------------------------------------------------------
// Section: PageTitle
// Purpose: Encapsulates the Page Title section of this file.
// -----------------------------------------------------------------------------
@Composable
fun PageTitle(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    HtmlTopBar(tr(title), subtitle?.let(::tr), onBack) {
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(tr(actionText), color = LocalMoneyMateTokens.current.action, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: MMCard
// Purpose: Encapsulates the MMCard section of this file.
// -----------------------------------------------------------------------------
@Composable
fun MMCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = HtmlCard(modifier = modifier, onClick = onClick, content = content)


// -----------------------------------------------------------------------------
// Section: SectionLabel
// Purpose: Encapsulates the Section Label section of this file.
// -----------------------------------------------------------------------------
@Composable
fun SectionLabel(text: String) = HtmlEyebrow(tr(text), Modifier.padding(horizontal = 20.dp, vertical = 8.dp))


// -----------------------------------------------------------------------------
// Section: MenuRow
// Purpose: Encapsulates the Menu Row section of this file.
// -----------------------------------------------------------------------------
@Composable
fun MenuRow(
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    leading: String = "•",
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).background(if (destructive) c.lightError else c.background, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(leading, color = if (destructive) c.error else c.action, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(tr(title), color = if (destructive) c.error else c.primaryText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = c.secondaryText, fontSize = 11.5.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (!badge.isNullOrBlank()) Text(badge, color = c.secondaryText, fontSize = 11.5.sp, modifier = Modifier.padding(end = 6.dp))
        Icon(Icons.Filled.ChevronRight, null, tint = c.mutedText, modifier = Modifier.size(19.dp))
    }
}


// -----------------------------------------------------------------------------
// Section: StatCard
// Purpose: Encapsulates the Stat Card section of this file.
// -----------------------------------------------------------------------------
@Composable
fun StatCard(label: String, value: String, positive: Boolean? = null, modifier: Modifier = Modifier) {
    val c = LocalMoneyMateTokens.current
    HtmlCard(modifier = modifier, padding = PaddingValues(14.dp)) {
        Text(label, color = c.secondaryText, fontSize = 11.5.sp)
        Spacer(Modifier.height(5.dp))
        Text(
            value,
            color = when (positive) { true -> c.success; false -> c.error; null -> c.primaryText },
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// -----------------------------------------------------------------------------
// Section: ProgressLine
// Purpose: Encapsulates the Progress Line section of this file.
// -----------------------------------------------------------------------------
@Composable
fun ProgressLine(progress: Float, warning: Boolean = false, exceeded: Boolean = false) {
    val c = LocalMoneyMateTokens.current
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = when { exceeded -> c.error; warning -> c.warning; else -> c.success },
        trackColor = c.divider
    )
}


// -----------------------------------------------------------------------------
// Section: Pill
// Purpose: Encapsulates the Pill section of this file.
// -----------------------------------------------------------------------------
@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit) = HtmlChip(text, selected, onClick)


// -----------------------------------------------------------------------------
// Section: PrimaryButton
// Purpose: Encapsulates the Primary Button section of this file.
// -----------------------------------------------------------------------------
@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = c.action, contentColor = Color.White)
    ) { Text(tr(text), fontWeight = FontWeight.Bold, fontSize = 15.sp) }
}


// -----------------------------------------------------------------------------
// Section: EmptyState
// Purpose: Encapsulates the Empty State section of this file.
// -----------------------------------------------------------------------------
@Composable
fun EmptyState(title: String, description: String) {
    val c = LocalMoneyMateTokens.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(56.dp).background(c.lightAction, CircleShape), contentAlignment = Alignment.Center) {
            Text(tr("—"), color = c.action, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(tr(title), color = c.primaryText, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(tr(description), color = c.secondaryText, fontSize = 13.sp)
    }
}
