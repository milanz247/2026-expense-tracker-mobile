package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(val icon: ImageVector, val title: String, val body: String)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.AccountBalanceWallet,
        title = "Track every rupee",
        body = "Log income, expenses, and transfers in seconds — your balances stay accurate automatically."
    ),
    OnboardingPage(
        icon = Icons.Default.Savings,
        title = "All your wallets, one place",
        body = "Bank accounts, cash, credit cards, and investments — see every balance side by side."
    ),
    OnboardingPage(
        icon = Icons.Default.Receipt,
        title = "Debts and store tabs, sorted",
        body = "Track who owes you, who you owe, and running tabs at your regular shops."
    ),
    OnboardingPage(
        icon = Icons.Default.Insights,
        title = "See where it goes",
        body = "Cash flow and category breakdowns on your dashboard, updated the moment you add something."
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val colors = LocalAppColors.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Box(modifier = Modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl, vertical = Spacing.md),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinished) {
                    Text("Skip", color = colors.textSecondary)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == index) 20.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) colors.accent else colors.outline)
                    )
                }
            }

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.xxl)
                    .height(52.dp)
            ) {
                Text(if (isLastPage) "Get Started" else "Next", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(colors.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(page.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))

        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = page.body,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}
