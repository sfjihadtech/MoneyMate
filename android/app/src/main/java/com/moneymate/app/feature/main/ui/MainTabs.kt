package com.moneymate.app.feature.main.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: MainTabs.kt
// Purpose: Primary Home, Activity, Insights, and Profile tab content plus transaction rows and filters.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.BuildConfig
import coil.compose.AsyncImage
import com.moneymate.app.core.common.CategoryCatalog
import com.moneymate.app.core.common.displayDate
import com.moneymate.app.core.common.money
import com.moneymate.app.data.model.Transaction
import com.moneymate.app.feature.main.MoneyMateState
import com.moneymate.app.feature.main.ToolPage
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch


// -----------------------------------------------------------------------------
// Section: HomeTab
// Purpose: Encapsulates the Home Tab section of this file.
// -----------------------------------------------------------------------------
@Composable
fun HomeTab(
    state: MoneyMateState,
    onOpenTool: (ToolPage) -> Unit,
    onAdd: () -> Unit,
    onViewActivity: () -> Unit,
    onProfile: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    var balanceVisible by remember { mutableStateOf(true) }
    val summary = state.dashboard.summary
    val month = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val year = LocalDate.now().year

    LazyColumn(
        Modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 14.dp, top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).background(c.brand, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).clickable(onClick = onProfile),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.user?.profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.user!!.profileImageUrl!!.let { if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it },
                            contentDescription = "Open profile and settings",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(tr("MM"), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Good ${greetingPart()}", color = c.mutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(state.user?.name ?: "MoneyMate User", color = c.primaryText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                HtmlIconButton(Icons.Filled.Notifications, "Notifications", { onOpenTool(ToolPage.NOTIFICATIONS) })
            }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        Modifier
                            .background(Brush.linearGradient(listOf(c.action, c.brand)))
                            .padding(horizontal = 22.dp, vertical = 22.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccountBalanceWallet, null, tint = Color.White.copy(alpha = .72f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(tr("Available Balance"), color = Color.White.copy(alpha = .72f), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { balanceVisible = !balanceVisible }, modifier = Modifier.size(34.dp)) {
                                    Icon(if (balanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Color.White.copy(alpha = .82f), modifier = Modifier.size(19.dp))
                                }
                            }
                            Text(
                                if (balanceVisible) money(summary.totalBalance, state.currency) else "••••••",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.4).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Schedule, null, tint = Color.White.copy(alpha = .5f), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr("Updated just now"), color = Color.White.copy(alpha = .5f), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                HeroStat("Total Income", money(summary.totalIncome, state.currency), Icons.Filled.ArrowDownward, c.lightSuccess, Modifier.weight(1f))
                                HeroStat("Total Expenses", money(summary.totalExpense, state.currency), Icons.Filled.ArrowUpward, c.error, Modifier.weight(1f))
                                HeroStat("Savings", money(summary.totalSavings, state.currency), Icons.Filled.Savings, Color(0xFF64B5F6), Modifier.weight(1f))
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    HtmlQuickAction(Icons.Filled.Add, "Add", onAdd, Modifier.weight(1f))
                    HtmlQuickAction(Icons.Filled.SwapHoriz, "Transfer", { onOpenTool(ToolPage.ACCOUNTS) }, Modifier.weight(1f))
                    HtmlQuickAction(Icons.Filled.PieChart, "Budget", { onOpenTool(ToolPage.BUDGETS) }, Modifier.weight(1f))
                    HtmlQuickAction(Icons.Filled.Savings, "Goals", { onOpenTool(ToolPage.GOALS) }, Modifier.weight(1f))
                }

                Spacer(Modifier.height(22.dp))
                HtmlSectionHeader(
                    title = mt(state,"Financial Health","ফাইন্যান্সিয়াল হেলথ"),
                    action = mt(state,"Details","বিস্তারিত"),
                    onAction = { onOpenTool(ToolPage.ANALYTICS_FULL) }
                )
                Spacer(Modifier.height(10.dp))
                HtmlCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { financialHealthScore(state) / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = c.success,
                                trackColor = c.divider,
                                strokeWidth = 7.dp
                            )
                            Text("${financialHealthScore(state)}", color = c.primaryText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(healthLabel(financialHealthScore(state)), color = c.primaryText, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(tr("Based on savings rate, budget adherence and bill status."), color = c.secondaryText, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(mt(state,"Monthly Overview","মাসিক ওভারভিউ"), color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    HtmlChip("$month $year", true, onClick = {})
                }
                Spacer(Modifier.height(10.dp))
                HtmlCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(c.success, mt(state,"Income","আয়"))
                        Spacer(Modifier.width(14.dp))
                        LegendDot(c.error, mt(state,"Expense","খরচ"))
                    }
                    Spacer(Modifier.height(18.dp))
                    HomeTrendChart(state)
                }

                Spacer(Modifier.height(22.dp))
                HtmlSectionHeader(
                    title = mt(state,"Budget Progress","বাজেট অগ্রগতি"),
                    action = "View All",
                    onAction = { onOpenTool(ToolPage.BUDGETS) }
                )
                Spacer(Modifier.height(10.dp))
                if (state.budgetProgress.budgets.isEmpty()) {
                    HtmlCard { EmptyState("No budgets yet", "Create a budget to track category spending.") }
                } else {
                    state.budgetProgress.budgets.take(3).forEach { budget ->
                        HtmlCard(Modifier.padding(bottom = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(38.dp).background(c.lightAction, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.PieChart, null, tint = c.action, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(budget.categoryName, color = c.primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${money(budget.spent, state.currency)} of ${money(budget.budgetAmount, state.currency)}", color = c.secondaryText, fontSize = 11.5.sp)
                                }
                                Text("${budget.percentage.toInt()}%", color = statusColor(budget.status), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.height(12.dp))
                            ProgressLine((budget.percentage / 100).toFloat(), budget.status == "warning", budget.status == "exceeded")
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                HtmlSectionHeader(
                    title = mt(state,"Recent Transactions","সাম্প্রতিক লেনদেন"),
                    action = "View All",
                    onAction = onViewActivity
                )
                Spacer(Modifier.height(10.dp))
                HtmlCard(padding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    if (state.transactions.isEmpty()) EmptyState("No transactions found", "Add your first transaction to see activity here.")
                    else state.transactions.take(5).forEachIndexed { index, tx ->
                        TransactionRow(state, tx, onClick = {})
                        if (index < state.transactions.take(5).lastIndex) HtmlDivider()
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HeroStat
// Purpose: Encapsulates the Hero Stat section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun HeroStat(label: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(15.dp), color = Color.White.copy(alpha = .12f)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text(label, color = accent, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HomeTrendChart
// Purpose: Encapsulates the Home Trend Chart section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun HomeTrendChart(state: MoneyMateState) {
    val c=LocalMoneyMateTokens.current
    val income=listOf(34f,22f,46f,19f,18f,92f,18f)
    val expense=listOf(20f,24f,26f,23f,29f,27f,23f)
    Canvas(Modifier.fillMaxWidth().height(148.dp)){
        fun points(values:List<Float>):Path{
            val p=Path(); val step=size.width/(values.size-1); val max=100f
            values.forEachIndexed{i,v->val x=i*step;val y=size.height-(v/max)*size.height*.82f-size.height*.08f;if(i==0)p.moveTo(x,y) else p.lineTo(x,y)};return p
        }
        val p1=points(income); val p2=points(expense)
        val area=Path().apply{addPath(p1);lineTo(size.width,size.height);lineTo(0f,size.height);close()}
        drawPath(area,brush=Brush.verticalGradient(listOf(c.success.copy(.18f),Color.Transparent)))
        drawPath(p1,color=c.success,style=Stroke(width=3.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round,join=androidx.compose.ui.graphics.StrokeJoin.Round))
        drawPath(p2,color=c.error,style=Stroke(width=3.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round,join=androidx.compose.ui.graphics.StrokeJoin.Round))
    }
    Row(horizontalArrangement=Arrangement.spacedBy(18.dp)){LegendDot(c.success,mt(state,"Income","আয়"));LegendDot(c.error,mt(state,"Expense","খরচ"))}
}


// -----------------------------------------------------------------------------
// Section: AnalyticsBarChart
// Purpose: Encapsulates the Analytics Bar Chart section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun AnalyticsBarChart(state: MoneyMateState) {
    val c=LocalMoneyMateTokens.current
    val months=listOf("Jan","Feb","Mar","Apr","May","Jun")
    val inc=listOf(.72f,.75f,.86f,.80f,.88f,.88f)
    val exp=listOf(.44f,.48f,.41f,.50f,.46f,.43f)
    Canvas(Modifier.fillMaxWidth().height(160.dp)){
        val group=size.width/months.size; val bw=group*.24f
        months.indices.forEach{i->
            val x=i*group+group*.24f
            val hi=size.height*.78f*inc[i]; val he=size.height*.78f*exp[i]
            drawRoundRect(c.success,topLeft=androidx.compose.ui.geometry.Offset(x,size.height-hi-18.dp.toPx()),size=androidx.compose.ui.geometry.Size(bw,hi),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            drawRoundRect(c.error,topLeft=androidx.compose.ui.geometry.Offset(x+bw+5.dp.toPx(),size.height-he-18.dp.toPx()),size=androidx.compose.ui.geometry.Size(bw,he),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
        }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround){months.forEach{Text(it,color=c.mutedText,fontSize=9.5.sp)}}
    Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(18.dp)){LegendDot(c.success,mt(state,"Income","আয়"));LegendDot(c.error,mt(state,"Expense","খরচ"))}
}


// -----------------------------------------------------------------------------
// Section: DonutBreakdown
// Purpose: Encapsulates the Donut Breakdown section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun DonutBreakdown(state: MoneyMateState) {
    val c=LocalMoneyMateTokens.current
    val data=state.categoryBreakdown.categories.take(6)
    val colors=listOf(c.warning,c.success,c.action,c.error,c.brand,c.secondaryText)
    val total=data.sumOf{it.amount}.coerceAtLeast(1.0)
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        Box(Modifier.size(150.dp),contentAlignment=Alignment.Center){
            Canvas(Modifier.fillMaxSize().padding(8.dp)){var start=-90f;data.forEachIndexed{i,d->val sweep=(d.amount/total*360).toFloat();drawArc(colors[i%colors.size],start,sweep,false,style=Stroke(width=18.dp.toPx()));start+=sweep}}
            Column(horizontalAlignment=Alignment.CenterHorizontally){Text(money(total,state.currency),color=c.primaryText,fontWeight=FontWeight.ExtraBold,fontSize=17.sp);Text(tr("Total spent"),color=c.mutedText,fontSize=9.5.sp)}
        }
        Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){data.forEachIndexed{i,d->Row(Modifier.padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).background(colors[i%colors.size],CircleShape));Spacer(Modifier.width(6.dp));Text(d.name,color=c.secondaryText,fontSize=11.5.sp,modifier=Modifier.weight(1f));Text("${d.percentage.toInt()}%",color=c.secondaryText,fontSize=11.5.sp,fontWeight=FontWeight.ExtraBold)}}}
    }
}


// -----------------------------------------------------------------------------
// Section: SavingsAreaChart
// Purpose: Encapsulates the Savings Area Chart section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun SavingsAreaChart() {
    val c=LocalMoneyMateTokens.current
    val vals=listOf(.42f,.50f,.45f,.63f,.73f,.67f,.86f)
    Canvas(Modifier.fillMaxWidth().height(120.dp)){
        val step=size.width/(vals.size-1);val p=Path();vals.forEachIndexed{i,v->val x=i*step;val y=size.height-v*size.height*.75f;if(i==0)p.moveTo(x,y) else p.lineTo(x,y)}
        val area=Path().apply{addPath(p);lineTo(size.width,size.height);lineTo(0f,size.height);close()}
        drawPath(area,brush=Brush.verticalGradient(listOf(c.action.copy(.25f),Color.Transparent)));drawPath(p,color=c.action,style=Stroke(width=2.5.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round,join=androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}


// -----------------------------------------------------------------------------
// Section: LegendDot
// Purpose: Encapsulates the Legend Dot section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun LegendDot(color: Color, text: String) {
    val c = LocalMoneyMateTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(text, color = c.secondaryText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}


// -----------------------------------------------------------------------------
// Section: TransactionsTab
// Purpose: Encapsulates the Transactions Tab section of this file.
// -----------------------------------------------------------------------------
@Composable
fun TransactionsTab(
    state: MoneyMateState,
    onAdd: () -> Unit,
    onEdit: (Transaction) -> Unit,
    externalFilter: String,
    onFilter: (String) -> Unit
) {
    val c = LocalMoneyMateTokens.current
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }
    LaunchedEffect(externalFilter) {
        selected = when (externalFilter.lowercase()) {
            "income" -> mt(state,"Income","আয়")
            "expense" -> mt(state,"Expense","খরচ")
            else -> "All"
        }
    }
    val filtered = state.transactions.filter { tx ->
        val category = state.categories.firstOrNull { it.id == tx.categoryId }?.name.orEmpty()
        val matchesQuery = query.isBlank() || tx.merchant.orEmpty().contains(query, true) || tx.notes.orEmpty().contains(query, true) || category.contains(query, true)
        val matchesType = selected == "All" || selected.equals(tx.type, true) || (selected != mt(state,"Income","আয়") && selected != mt(state,"Expense","খরচ") && category.equals(selected, true))
        matchesQuery && matchesType
    }
    val categoryChips = listOf("All", mt(state,"Income","আয়"), mt(state,"Expense","খরচ")) +
        CategoryCatalog.ordered(state.categories).map { it.name }.distinct()

    LazyColumn(Modifier.fillMaxSize().background(c.background), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            HtmlTopBar(mt(state,"Transaction History","লেনদেনের ইতিহাস")) {
                HtmlIconButton(Icons.Filled.Tune, "Filters", onClick = { onFilter(selected) })
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = c.surface, border = androidx.compose.foundation.BorderStroke(1.dp, c.border)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Search, null, tint = c.mutedText, modifier = Modifier.size(20.dp))
                        TextField(
                            query, { query = it },
                            placeholder = { Text(mt(state,"Search transactions","লেনদেন খুঁজুন"), color = c.mutedText, fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = c.action,
                                focusedTextColor = c.primaryText,
                                unfocusedTextColor = c.primaryText
                            )
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryChips.forEach { chip -> HtmlChip(chip, selected == chip, { selected = chip }) }
                }
            }
        }
        if (filtered.isEmpty()) item { EmptyState("No transactions found", "Try adjusting your search or filters.") }
        else {
            items(filtered, key = { it.id }) { tx ->
                SwipeTransactionItem(
                    state = state,
                    tx = tx,
                    onEdit = { onEdit(tx) }
                )
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: SwipeTransactionItem
// Purpose: Swipe right to reveal Edit; swipe left to reveal Delete.
// Transfer history is protected because a transfer is a paired operation.
// -----------------------------------------------------------------------------
@Composable
private fun SwipeTransactionItem(
    state: MoneyMateState,
    tx: Transaction,
    onEdit: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var offsetX by remember(tx.id) { mutableFloatStateOf(0f) }
    var confirmDelete by remember(tx.id) { mutableStateOf(false) }
    val protectedTransfer = tx.type.equals("transfer", true)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        Row(Modifier.matchParentSize()) {
            Surface(
                modifier = Modifier.width(92.dp).fillMaxHeight().clickable(enabled = !protectedTransfer) {
                    offsetX = 0f
                    onEdit()
                },
                color = c.action
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(tr("Edit"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Surface(
                modifier = Modifier.width(92.dp).fillMaxHeight().clickable(enabled = !protectedTransfer) {
                    offsetX = 0f
                    confirmDelete = true
                },
                color = c.error
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(tr("Delete"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        HtmlCard(
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(tx.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = when {
                                offsetX > 42f -> 92f
                                offsetX < -42f -> -92f
                                else -> 0f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!protectedTransfer) offsetX = (offsetX + dragAmount).coerceIn(-92f, 92f)
                        }
                    )
                },
            padding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            TransactionRow(state, tx, onClick = if (protectedTransfer) null else onEdit)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(tr("Delete transaction?")) },
            text = { Text(tr("This will permanently delete this transaction and update the account balance.")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch { state.deleteTransaction(tx.id) }
                }) { Text(tr("Delete"), color = c.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(tr("Cancel")) } }
        )
    }
}


// -----------------------------------------------------------------------------
// Section: TransactionRow
// Purpose: Encapsulates the Transaction Row section of this file.
// -----------------------------------------------------------------------------
@Composable
fun TransactionRow(state: MoneyMateState, tx: Transaction, onClick: (() -> Unit)? = null) {
    val c = LocalMoneyMateTokens.current
    val category = state.categories.firstOrNull { it.id == tx.categoryId }
    val income = tx.type.equals("income", true)
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 11.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).background(if (income) c.lightSuccess else c.lightAction, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (income) Icons.Filled.ArrowDownward else categoryIcon(category?.name), null, tint = if (income) c.success else c.action, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.merchant ?: category?.name ?: "Transaction", color = c.primaryText, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${category?.name ?: tx.type.replaceFirstChar(Char::uppercase)} · ${displayDate(tx.occurredAt)}", color = c.secondaryText, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            (if (income) "+" else "−") + money(tx.amount, state.currency),
            color = if (income) c.success else c.primaryText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}


// -----------------------------------------------------------------------------
// Section: AnalyticsTab
// Purpose: Encapsulates the Analytics Tab section of this file.
// -----------------------------------------------------------------------------
@Composable
fun AnalyticsTab(state: MoneyMateState, onOpenTool: (ToolPage) -> Unit) {
    val c = LocalMoneyMateTokens.current
    var range by remember { mutableStateOf(mt(state,"This Month","এই মাস")) }
    val categories = state.categoryBreakdown.categories

    LazyColumn(Modifier.fillMaxSize().background(c.background), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            HtmlTopBar(mt(state,"Analytics","অ্যানালিটিক্স")) {
                HtmlIconButton(Icons.Filled.Share, "Export", { onOpenTool(ToolPage.EXPORT) })
            }
        }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(mt(state,"This Week","এই সপ্তাহ"), mt(state,"This Month","এই মাস"), mt(state,"This Year","এই বছর")).forEach { HtmlChip(it, range == it, { range = it }) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                HtmlCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(mt(state,"Income vs Expense","আয় বনাম খরচ"), color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        Text(mt(state,"Last 6 months","গত ৬ মাস"), color = c.secondaryText, fontSize = 11.5.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    AnalyticsBarChart(state)
                }
                Spacer(Modifier.height(22.dp))
                HtmlCard {
                    Text(mt(state,"Category breakdown","ক্যাটাগরি বিশ্লেষণ"), color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(14.dp))
                    if (categories.isEmpty()) Text(tr("No expense data yet."), color = c.secondaryText, fontSize = 13.sp) else DonutBreakdown(state)
                }
                Spacer(Modifier.height(22.dp))
                HtmlCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(mt(state,"Savings trend","সঞ্চয়ের প্রবণতা"), color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        val savingsRate = if (state.monthly.summary.totalIncome > 0) ((state.monthly.summary.netAmount / state.monthly.summary.totalIncome) * 100).coerceIn(-999.0, 999.0) else 0.0
                        Text("${savingsRate.toInt()}%", color = if (savingsRate >= 0) c.success else c.error, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    SavingsAreaChart()
                }
                Spacer(Modifier.height(22.dp))
                Text(mt(state,"Insights","ইনসাইটস"), color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                HtmlCard(onClick = { onOpenTool(ToolPage.AI_INSIGHTS) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(c.lightAction, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = c.action)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(categories.firstOrNull()?.let { "${it.name} is your top spending category" } ?: "Not Enough Data Yet", color = c.primaryText, fontWeight = FontWeight.Bold)
                            Text(categories.firstOrNull()?.let { "It represents about ${it.percentage.toInt()}% of your spending." } ?: "Add transactions to generate personalized insights.", color = c.secondaryText, fontSize = 11.5.sp)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                HtmlCard(onClick = { onOpenTool(ToolPage.ADVANCED_ANALYTICS) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(c.lightSuccess, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Analytics, null, tint = c.success)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tr("Advanced Analytics"), color = c.primaryText, fontWeight = FontWeight.Bold)
                            Text(tr("Explore savings rate, budget adherence and spending concentration."), color = c.secondaryText, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: ProfileTab
// Purpose: Encapsulates the Profile Tab section of this file.
// -----------------------------------------------------------------------------
@Composable
fun ProfileTab(
    state: MoneyMateState,
    onOpenTool: (ToolPage) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val fullName = state.user?.name ?: "MoneyMate User"
    val email = state.user?.email.orEmpty()
    val profileImage = state.user?.profileImageUrl?.let { if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it }

    LazyColumn(Modifier.fillMaxSize().background(c.background), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { HtmlTopBar(mt(state,"My Profile","আমার প্রোফাইল")) }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(Brush.linearGradient(listOf(c.action, c.brand))), contentAlignment = Alignment.Center) {
                        if (!profileImage.isNullOrBlank()) AsyncImage(model = profileImage, contentDescription = "Profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape))
                        else Text(initials(fullName), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(fullName, color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        if (email.isNotBlank()) Text(email, color = c.secondaryText, fontSize = 12.5.sp)
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = c.lightAction,
                            modifier = Modifier.padding(top = 5.dp)
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Verified, null, tint = c.action, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(memberSince(state.user?.createdAt), color = c.action, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    HtmlIconButton(Icons.Filled.Edit, "Edit profile", { onOpenTool(ToolPage.EDIT_PROFILE) })
                }

                Spacer(Modifier.height(8.dp))
                PremiumStatusCard(
                    onViewPlans = { onOpenTool(ToolPage.PLANS) }
                )

                Spacer(Modifier.height(4.dp))
                HtmlEyebrow("Financial Tools", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow("My Accounts", "${state.accounts.size} accounts", Icons.Filled.AccountBalanceWallet, onClick = { onOpenTool(ToolPage.ACCOUNTS) }); HtmlDivider()
                    HtmlSettingsRow("Monthly Budget", null, Icons.Filled.PieChart, onClick = { onOpenTool(ToolPage.BUDGETS) }); HtmlDivider()
                    HtmlSettingsRow("Savings Goals", null, Icons.Filled.Savings, onClick = { onOpenTool(ToolPage.GOALS) }); HtmlDivider()
                    HtmlSettingsRow("Bills", null, Icons.Filled.ReceiptLong, onClick = { onOpenTool(ToolPage.BILLS) }); HtmlDivider()
                    HtmlSettingsRow("Calendar", null, Icons.Filled.CalendarMonth, onClick = { onOpenTool(ToolPage.CALENDAR) }); HtmlDivider()
                    HtmlSettingsRow("Advanced Analytics", null, Icons.Filled.Analytics, onClick = { onOpenTool(ToolPage.ADVANCED_ANALYTICS) }); HtmlDivider()
                    HtmlSettingsRow("AI Financial Insights", "Private insights from your data", Icons.Filled.AutoAwesome, onClick = { onOpenTool(ToolPage.AI_INSIGHTS) }); HtmlDivider()
                    HtmlSettingsRow("Recurring Transactions", null, Icons.Filled.Repeat, onClick = { onOpenTool(ToolPage.RECURRING) }); HtmlDivider()
                    HtmlSettingsRow("Advanced Search & Filters", null, Icons.Filled.FilterAlt, onClick = { onOpenTool(ToolPage.ADVANCED_FILTERS) })
                }

                Spacer(Modifier.height(22.dp)); HtmlEyebrow("General", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow(
                        "Appearance",
                        "Dark mode",
                        Icons.Filled.DarkMode,
                        trailing = {
                            Switch(
                                checked = state.prefs.darkMode,
                                onCheckedChange = { enabled ->
                                    state.prefs.darkMode = enabled
                                    onDarkModeChange(enabled)
                                }
                            )
                        }
                    ); HtmlDivider()
                    HtmlSettingsRow("Language", if (state.language == "bn") "বাংলা" else "English", Icons.Filled.Language, onClick = { onOpenTool(ToolPage.LANGUAGE) }); HtmlDivider()
                    HtmlSettingsRow("Currency", state.currency, Icons.Filled.AttachMoney, onClick = { onOpenTool(ToolPage.CURRENCY) }); HtmlDivider()
                    HtmlSettingsRow("Push Notifications", if (state.prefs.pushNotifications) "Enabled" else "Disabled", Icons.Filled.Notifications, trailing = { Switch(state.prefs.pushNotifications, { state.prefs.pushNotifications = it }) }); HtmlDivider()
                    HtmlSettingsRow("Accessibility", "High contrast, large text", Icons.Filled.AccessibilityNew, onClick = { onOpenTool(ToolPage.ACCESSIBILITY) }); HtmlDivider()
                    HtmlSettingsRow("Premium Themes", state.selectedPremiumTheme, Icons.Filled.Palette, onClick = { onOpenTool(ToolPage.PREMIUM_THEMES) }); HtmlDivider()
                    HtmlSettingsRow("Premium App Icons", state.selectedAppIcon, Icons.Filled.Apps, onClick = { onOpenTool(ToolPage.PREMIUM_ICONS) })
                }

                Spacer(Modifier.height(22.dp)); HtmlEyebrow("Security", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow(
                        "PIN Lock",
                        if (state.prefs.hasPin()) "On" else "Off",
                        Icons.Filled.Pin,
                        trailing = { Switch(state.prefs.hasPin(), { onOpenTool(ToolPage.SECURITY) }) }
                    ); HtmlDivider()
                    HtmlSettingsRow(
                        "Biometric Authentication",
                        "Requires PIN Lock to be set up first",
                        Icons.Filled.Fingerprint,
                        trailing = { Switch(state.prefs.biometricEnabled, { onOpenTool(ToolPage.SECURITY) }) }
                    ); HtmlDivider()
                    HtmlSettingsRow(
                        "Auto Lock",
                        "After 1 minute",
                        Icons.Filled.LockClock,
                        trailing = { Switch(true, { onOpenTool(ToolPage.SECURITY) }) }
                    )
                }

                Spacer(Modifier.height(22.dp)); HtmlEyebrow("Data", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow("Backup", "Create or download a secure backup", Icons.Filled.CloudUpload, onClick = { onOpenTool(ToolPage.BACKUP) }); HtmlDivider()
                    HtmlSettingsRow("Restore", null, Icons.Filled.Restore, onClick = { onOpenTool(ToolPage.RESTORE) }); HtmlDivider()
                    HtmlSettingsRow("Export Data (CSV)", null, Icons.Filled.FileDownload, onClick = { onOpenTool(ToolPage.EXPORT) }); HtmlDivider()
                    HtmlSettingsRow("Export Data (PDF)", null, Icons.Filled.PictureAsPdf, onClick = { onOpenTool(ToolPage.EXPORT) }); HtmlDivider()
                    HtmlSettingsRow("Import Data", null, Icons.Filled.UploadFile, onClick = { onOpenTool(ToolPage.IMPORT) }); HtmlDivider()
                    HtmlSettingsRow(
                        "Cloud Sync",
                        if (state.guestMode) "Unavailable in Guest Mode" else "Automatic • synced with your MoneyMate account",
                        Icons.Filled.Sync,
                        trailing = { Icon(if (state.guestMode) Icons.Filled.CloudOff else Icons.Filled.CloudDone, null, tint = if (state.guestMode) c.mutedText else c.success) }
                    )
                }

                Spacer(Modifier.height(22.dp)); HtmlEyebrow("Help & Support", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow("FAQ", "Help topics & frequently asked questions", Icons.Filled.QuestionAnswer, onClick = { onOpenTool(ToolPage.FAQ) }); HtmlDivider()
                    HtmlSettingsRow("Contact Support", null, Icons.Filled.ContactSupport, onClick = { onOpenTool(ToolPage.CONTACT) })
                }

                Spacer(Modifier.height(22.dp)); HtmlEyebrow("Legal", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow("Privacy", null, Icons.Filled.PrivacyTip, onClick = { onOpenTool(ToolPage.PRIVACY) }); HtmlDivider()
                    HtmlSettingsRow("Terms of Service", null, Icons.Filled.Gavel, onClick = { onOpenTool(ToolPage.TERMS) })
                }

                Spacer(Modifier.height(22.dp)); HtmlEyebrow("About", Modifier.padding(vertical = 8.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow("About MoneyMate", null, Icons.Filled.Info, onClick = { onOpenTool(ToolPage.ABOUT) })
                }

                Spacer(Modifier.height(22.dp))
                HtmlCard(padding = PaddingValues(horizontal = 6.dp)) {
                    HtmlSettingsRow("Sign Out", null, Icons.Filled.Logout, destructive = true, onClick = onSignOut)
                }
                Text(tr("MoneyMate v${BuildConfig.VERSION_NAME}"), color = c.mutedText, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}



// -----------------------------------------------------------------------------
// Section: PremiumStatusCard
// Purpose: Encapsulates the Premium Status Card section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun PremiumStatusCard(onViewPlans: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = c.brand,
        border = androidx.compose.foundation.BorderStroke(1.dp, c.warning.copy(alpha = .45f)),
        shadowElevation = 6.dp
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkspacePremium, null, tint = c.warning, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text(tr("MoneyMate Premium"), color = c.warning, fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Premium themes, icons and advanced tools are available through MoneyMate subscription plans.",
                color = Color.White.copy(alpha = .72f),
                fontSize = 12.5.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onViewPlans,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = c.brand),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(tr("View Plans"), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: memberSince
// Purpose: Encapsulates the member Since section of this file.
// -----------------------------------------------------------------------------
private fun memberSince(createdAt: String?): String {
    val date = runCatching { LocalDate.parse(createdAt?.take(10)) }.getOrNull()
        ?: return "MoneyMate member"
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    return "Member since $month ${date.year}"
}


// -----------------------------------------------------------------------------
// Section: greetingPart
// Purpose: Encapsulates the greeting Part section of this file.
// -----------------------------------------------------------------------------
private fun greetingPart(): String = when (LocalTime.now().hour) { in 5..11 -> "morning"; in 12..17 -> "afternoon"; else -> "evening" }

// -----------------------------------------------------------------------------
// Section: initials
// Purpose: Encapsulates the initials section of this file.
// -----------------------------------------------------------------------------
private fun initials(name: String): String = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "MM" }

// -----------------------------------------------------------------------------
// Section: financialHealthScore
// Purpose: Encapsulates the financial Health Score section of this file.
// -----------------------------------------------------------------------------
private fun financialHealthScore(state: MoneyMateState): Int {
    val income = state.monthly.summary.totalIncome
    val expense = state.monthly.summary.totalExpense
    val savingsRate = if (income > 0) ((income - expense) / income).coerceIn(-1.0, 1.0) else 0.0
    val budgetPenalty = state.budgetProgress.budgets.count { it.status == "exceeded" } * 8
    val overduePenalty = state.bills.count { it.status.equals("overdue", true) } * 10
    return (72 + (savingsRate * 22).toInt() - budgetPenalty - overduePenalty).coerceIn(0, 100)
}

// -----------------------------------------------------------------------------
// Section: healthLabel
// Purpose: Encapsulates the health Label section of this file.
// -----------------------------------------------------------------------------
private fun healthLabel(score: Int) = when { score >= 80 -> "Great standing"; score >= 65 -> "Good standing"; score >= 45 -> "Needs attention"; else -> "Action recommended" }

// -----------------------------------------------------------------------------
// Section: categoryIcon
// Purpose: Encapsulates the category Icon section of this file.
// -----------------------------------------------------------------------------
@Composable private fun statusColor(status: String): Color { val c = LocalMoneyMateTokens.current; return when (status) { "exceeded" -> c.error; "warning" -> c.warning; else -> c.success } }
private fun categoryIcon(name: String?): ImageVector = when {
    name?.contains("food", true) == true -> Icons.Filled.Restaurant
    name?.contains("grocer", true) == true -> Icons.Filled.ShoppingCart
    name?.contains("shop", true) == true -> Icons.Filled.ShoppingBag
    name?.contains("transport", true) == true -> Icons.Filled.DirectionsCar
    name?.contains("bill", true) == true -> Icons.Filled.ReceiptLong
    name?.contains("rent", true) == true || name?.contains("housing", true) == true -> Icons.Filled.Home
    name?.contains("entertain", true) == true -> Icons.Filled.Movie
    name?.contains("subscription", true) == true -> Icons.Filled.Subscriptions
    name?.contains("health", true) == true -> Icons.Filled.Favorite
    name?.contains("insurance", true) == true -> Icons.Filled.Shield
    name?.contains("travel", true) == true -> Icons.Filled.Flight
    name?.contains("education", true) == true -> Icons.Filled.School
    name?.contains("personal", true) == true -> Icons.Filled.Spa
    name?.contains("kids", true) == true || name?.contains("family", true) == true -> Icons.Filled.FamilyRestroom
    name?.contains("pet", true) == true -> Icons.Filled.Pets
    name?.contains("gift", true) == true || name?.contains("donation", true) == true -> Icons.Filled.VolunteerActivism
    name?.contains("tax", true) == true -> Icons.Filled.AccountBalance
    name?.contains("salary", true) == true -> Icons.Filled.Payments
    name?.contains("freelance", true) == true -> Icons.Filled.LaptopMac
    name?.contains("business", true) == true -> Icons.Filled.Store
    name?.contains("investment", true) == true -> Icons.Filled.TrendingUp
    name?.contains("rental", true) == true -> Icons.Filled.Apartment
    name?.contains("refund", true) == true -> Icons.Filled.Replay
    else -> Icons.Filled.Category
}


// -----------------------------------------------------------------------------
// Section: mt
// Purpose: Encapsulates the mt section of this file.
// -----------------------------------------------------------------------------
private fun mt(state: MoneyMateState, en: String, bn: String): String = if (state.language == "bn") bn else en
