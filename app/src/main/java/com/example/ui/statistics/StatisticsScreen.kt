package com.example.ui.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CashFlowPoint
import com.example.network.CategoryBreakdownItem
import com.example.network.MetricWithTrend
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.SectionLabel
import com.example.ui.common.formatMoneyCompact
import com.example.ui.common.parseHexColor
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

/**
 * A deeper view over the exact same `reports/summary` data Dashboard shows compressed — full
 * category breakdown (not capped to 6) and a full-width cash-flow chart. No new endpoint: this
 * reuses [DashboardViewModel] as-is, so opening it just re-fetches the same summary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: DashboardViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val summary by viewModel.summary.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Statistics", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).windowInsetsPadding(WindowInsets.safeDrawing)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
                contentPadding = PaddingValues(top = Spacing.lg, bottom = 96.dp)
            ) {
                if (errorMessage != null) {
                    item { ErrorBanner(errorMessage!!) }
                }

                if (isLoading && summary == null) {
                    item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
                }

                summary?.let { s ->
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            StatTile("Income", s.totalIncome, userCurrency, colors.positive, Modifier.weight(1f))
                            StatTile("Expense", s.totalExpense, userCurrency, colors.negative, Modifier.weight(1f))
                            StatTile("Net", s.netBalance, userCurrency, colors.accent, Modifier.weight(1f))
                        }
                    }

                    if (s.cashFlow.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SectionLabel("Cash Flow")
                                MatteCard(cornerRadius = 24, contentPadding = PaddingValues(Spacing.xl)) {
                                    FullCashFlowChart(s.cashFlow, colors.accent, colors.outline, colors.textMuted)
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            SectionLabel("Spend by Category")
                            if (s.categoryBreakdown.isEmpty()) {
                                EmptyState("No category data yet.")
                            } else {
                                MatteCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                        s.categoryBreakdown.forEach { item -> CategoryStatRow(item) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, metric: MetricWithTrend, currency: String, tint: Color, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    MatteCard(modifier = modifier, cornerRadius = 18, contentPadding = PaddingValues(Spacing.md)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(text = label.uppercase(), fontSize = 9.sp, color = colors.textMuted, letterSpacing = 1.sp, fontFamily = GeistMono)
            Text(
                text = formatMoneyCompact(metric.amountCents, currency),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
                fontFamily = GeistMono,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullCashFlowChart(data: List<CashFlowPoint>, incomeColor: Color, expenseColor: Color, labelColor: Color) {
    val maxValue = remember(data) { (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            LegendDot("Income", incomeColor)
            LegendDot("Expense", expenseColor)
        }
        Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            data.forEachIndexed { index, point ->
                val growth = remember(point) { Animatable(0f) }
                LaunchedEffect(point) {
                    kotlinx.coroutines.delay(index * 45L)
                    growth.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
                }
                Column(modifier = Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                        Box(modifier = Modifier.width(8.dp).fillMaxHeight(fraction = (growth.value * point.income.toFloat() / maxValue).coerceIn(0.02f, 1f)).clip(RoundedCornerShape(3.dp)).background(incomeColor))
                        Box(modifier = Modifier.width(8.dp).fillMaxHeight(fraction = (growth.value * point.expense.toFloat() / maxValue).coerceIn(0.02f, 1f)).clip(RoundedCornerShape(3.dp)).background(expenseColor))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = point.month, fontSize = 10.sp, color = labelColor, fontFamily = GeistMono)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    val colors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, fontSize = 11.sp, color = colors.textSecondary)
    }
}

@Composable
private fun CategoryStatRow(item: CategoryBreakdownItem) {
    val colors = LocalAppColors.current
    val tint = parseHexColor(item.color)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(tint))
            Text(text = item.categoryName, fontSize = 13.sp, color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(text = "${String.format("%.1f", item.percentage)}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary, fontFamily = GeistMono)
        }
        LinearProgressIndicator(
            progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = tint,
            trackColor = colors.outline
        )
    }
}
