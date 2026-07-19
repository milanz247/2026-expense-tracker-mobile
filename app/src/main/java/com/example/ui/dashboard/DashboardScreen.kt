package com.example.ui.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.AccountResponse
import com.example.network.CashFlowPoint
import com.example.network.CategoryBreakdownItem
import com.example.network.MetricWithTrend
import com.example.network.RecentTransaction
import com.example.network.TXN_TYPE_EXPENSE
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.MatteCard
import com.example.ui.common.SectionLabel
import com.example.ui.common.formatMoney
import com.example.ui.common.formatMoneyCompact
import com.example.ui.common.iconForAccountType
import com.example.ui.common.parseHexColor
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSeeAllTransactions: () -> Unit,
    onAddTransaction: () -> Unit,
    onSearch: () -> Unit,
    onSeeStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val userName by viewModel.userName.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(text = "Dashboard", fontWeight = FontWeight.Bold, color = colors.onBackground)
                        Text(
                            text = if (userName.isBlank()) "Welcome back" else "Welcome back, $userName",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search transactions", tint = colors.textMuted)
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.textMuted)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onAddTransaction,
                containerColor = colors.accent,
                contentColor = colors.onAccent
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
                contentPadding = PaddingValues(top = Spacing.lg, bottom = 96.dp)
            ) {
                if (errorMessage != null) {
                    item { ErrorBanner(errorMessage!!) }
                }

                summary?.let { s ->
                    item { HeroBalanceCard(s.netBalance, s.totalIncome, s.totalExpense, userCurrency) }

                    if (accounts.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionLabel("Wallets")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                    items(accounts, key = { it.id }) { account -> WalletChip(account, userCurrency) }
                                }
                            }
                        }
                    }

                    if (s.cashFlow.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SectionLabel("Cash Flow")
                                    Text(
                                        text = "View Stats",
                                        fontSize = 12.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { onSeeStatistics() }
                                    )
                                }
                                MatteCard { CashFlowBarChart(s.cashFlow) }
                            }
                        }
                    }

                    if (s.categoryBreakdown.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionLabel("Spend by Category")
                                MatteCard { CategoryBreakdownRow(s.categoryBreakdown) }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel("Recent Activity")
                                Text(
                                    text = "See all",
                                    fontSize = 12.sp,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { onSeeAllTransactions() }
                                )
                            }
                            if (s.recentTransactions.isEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                    EmptyState("No transactions yet.")
                                    ExtendedFloatingActionButton(
                                        text = { Text("Add your first transaction") },
                                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                        onClick = onAddTransaction,
                                        containerColor = colors.surfaceVariant,
                                        contentColor = colors.onBackground,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    items(s.recentTransactions, key = { it.id }) { txn ->
                        RecentActivityRow(txn, userCurrency, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    net: MetricWithTrend,
    income: MetricWithTrend,
    expense: MetricWithTrend,
    currency: String
) {
    val colors = LocalAppColors.current
    MatteCard(cornerRadius = 28, contentPadding = PaddingValues(Spacing.xxl)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(text = "NET BALANCE", fontSize = 11.sp, color = colors.textMuted, letterSpacing = 1.sp, fontFamily = GeistMono)
                Text(
                    text = formatMoney(animatedCents(net.amountCents), currency),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                    fontFamily = GeistMono,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                HeroStat("Income", income, currency, Modifier.weight(1f))
                HeroStat("Expense", expense, currency, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, metric: MetricWithTrend, currency: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(text = label.uppercase(), fontSize = 10.sp, color = colors.textMuted, letterSpacing = 1.sp, fontFamily = GeistMono)
        Text(
            text = formatMoneyCompact(animatedCents(metric.amountCents), currency),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            fontFamily = GeistMono,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val change = metric.changePercent
        if (change != null) {
            val positive = change >= 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(
                    imageVector = if (positive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = if (positive) "Up" else "Down",
                    tint = if (positive) colors.positive else colors.negative,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${String.format("%.1f", kotlin.math.abs(change))}%",
                    fontSize = 11.sp,
                    color = if (positive) colors.positive else colors.negative
                )
            }
        }
    }
}

/** Animates a cents value counting up from 0 to [target] whenever [target] changes (e.g. on refresh). */
@Composable
private fun animatedCents(target: Long): Long {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }
    return (progress.value.toDouble() * target).roundToLong()
}

@Composable
private fun WalletChip(account: AccountResponse, currency: String) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.outline, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForAccountType(account.type), contentDescription = null, tint = colors.accent, modifier = Modifier.size(15.dp))
            }
            Text(
                text = account.name,
                fontSize = 12.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatMoneyCompact(account.balance, currency),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                fontFamily = GeistMono
            )
        }
    }
}

@Composable
private fun CashFlowBarChart(data: List<CashFlowPoint>, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val maxValue = remember(data) { (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEachIndexed { index, point ->
            val growth = remember(point) { Animatable(0f) }
            LaunchedEffect(point) {
                kotlinx.coroutines.delay(index * 45L)
                growth.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(fraction = (growth.value * point.income.toFloat() / maxValue).coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.accent)
                    )
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(fraction = (growth.value * point.expense.toFloat() / maxValue).coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.outline)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = point.month, fontSize = 10.sp, color = colors.textMuted, fontFamily = GeistMono)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(data: List<CategoryBreakdownItem>) {
    val colors = LocalAppColors.current
    val sweepProgress = remember(data) { Animatable(0f) }
    LaunchedEffect(data) {
        sweepProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Canvas(modifier = Modifier.size(96.dp)) {
            var startAngle = -90f
            val strokeWidth = size.minDimension * 0.22f
            data.forEach { item ->
                val sweep = (item.percentage / 100f * 360f).toFloat().coerceAtLeast(0f) * sweepProgress.value
                drawArc(
                    color = parseHexColor(item.color),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
                startAngle += (item.percentage / 100f * 360f).toFloat().coerceAtLeast(0f)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            data.take(6).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(item.color))
                    )
                    Text(
                        text = item.categoryName,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${String.format("%.0f", item.percentage)}%",
                        fontSize = 12.sp,
                        color = colors.onBackground,
                        fontFamily = GeistMono
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(transaction: RecentTransaction, currency: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isExpense = transaction.type == TXN_TYPE_EXPENSE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(transaction.category?.color?.let { parseHexColor(it).copy(alpha = 0.2f) } ?: colors.outline),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(transaction.category?.color?.let { parseHexColor(it) } ?: colors.textMuted)
                )
            }
            Column {
                Text(
                    text = transaction.description.ifBlank { transaction.category?.name ?: transaction.type },
                    fontSize = 14.sp,
                    color = colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.category?.name ?: transaction.type,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }
        Text(
            text = (if (isExpense) "-" else "+") + formatMoney(transaction.amount, currency),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) colors.textSecondary else colors.positive,
            fontFamily = GeistMono
        )
    }
}
