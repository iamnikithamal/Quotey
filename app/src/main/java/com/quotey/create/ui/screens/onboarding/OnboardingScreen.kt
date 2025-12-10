package com.quotey.create.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quotey.create.ui.theme.Primary
import com.quotey.create.ui.theme.PrimaryContainer
import com.quotey.create.ui.theme.PrimaryDark
import com.quotey.create.ui.theme.Secondary
import com.quotey.create.ui.theme.SecondaryContainer
import com.quotey.create.ui.theme.Tertiary
import com.quotey.create.ui.theme.TertiaryContainer
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Rounded.FormatQuote,
        title = "Welcome to Quotey",
        description = "Transform your words into stunning visual masterpieces. Create beautiful quote images for social media in seconds.",
        primaryColor = Primary,
        secondaryColor = PrimaryContainer,
        accentColor = PrimaryDark
    ),
    OnboardingPage(
        icon = Icons.Rounded.ColorLens,
        title = "Endless Customization",
        description = "Choose from beautiful gradients, solid colors, patterns, and abstract backgrounds. Customize fonts, sizes, and positions with precision.",
        primaryColor = Secondary,
        secondaryColor = SecondaryContainer,
        accentColor = Secondary
    ),
    OnboardingPage(
        icon = Icons.Rounded.AutoAwesome,
        title = "Multiple Pages",
        description = "Create multi-slide presentations for carousel posts. Perfect for breaking down essays and long-form content.",
        primaryColor = Tertiary,
        secondaryColor = TertiaryContainer,
        accentColor = Tertiary
    ),
    OnboardingPage(
        icon = Icons.Rounded.Share,
        title = "Export Anywhere",
        description = "Export in any aspect ratio for Instagram, Facebook, Twitter, Pinterest, and more. High-quality output for every platform.",
        primaryColor = Primary,
        secondaryColor = PrimaryContainer,
        accentColor = Secondary
    )
)

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated background
            OnboardingBackground(
                currentPage = pagerState.currentPage,
                pageOffset = pagerState.currentPageOffsetFraction
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Skip button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    AnimatedVisibility(
                        visible = pagerState.currentPage < onboardingPages.lastIndex
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.completeOnboarding()
                                onOnboardingComplete()
                            }
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    OnboardingPageContent(
                        page = onboardingPages[page],
                        isCurrentPage = pagerState.currentPage == page
                    )
                }

                // Page indicators and navigation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(onboardingPages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "indicator_width"
                            )
                            val alpha by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0.3f,
                                animationSpec = tween(300),
                                label = "indicator_alpha"
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(width)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation button
                    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

                    Button(
                        onClick = {
                            if (isLastPage) {
                                viewModel.completeOnboarding()
                                onOnboardingComplete()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        AnimatedContent(
                            targetState = isLastPage,
                            transitionSpec = {
                                fadeIn(tween(200)) + slideInHorizontally { it / 2 } togetherWith
                                        fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
                            },
                            label = "button_content"
                        ) { lastPage ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (lastPage) {
                                    Text(
                                        text = "Get Started",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Next",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
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
private fun OnboardingPageContent(
    page: OnboardingPage,
    isCurrentPage: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with animated background
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            page.primaryColor.copy(alpha = 0.2f),
                            page.secondaryColor.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = page.primaryColor
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )
    }
}

@Composable
private fun OnboardingBackground(
    currentPage: Int,
    pageOffset: Float
) {
    val currentColors = onboardingPages[currentPage]
    val nextColors = onboardingPages.getOrNull(currentPage + 1) ?: currentColors

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Blend colors based on page offset
        val blendedPrimary = lerp(
            currentColors.primaryColor,
            nextColors.primaryColor,
            pageOffset.coerceIn(0f, 1f)
        )
        val blendedSecondary = lerp(
            currentColors.secondaryColor,
            nextColors.secondaryColor,
            pageOffset.coerceIn(0f, 1f)
        )

        // Top-right decorative blob
        val blobPath = Path().apply {
            moveTo(width * 0.7f, 0f)
            cubicTo(
                width * 0.9f, height * 0.1f,
                width * 1.1f, height * 0.15f,
                width, height * 0.25f
            )
            lineTo(width, 0f)
            close()
        }

        drawPath(
            path = blobPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    blendedPrimary.copy(alpha = 0.15f),
                    blendedSecondary.copy(alpha = 0.1f)
                ),
                start = Offset(width * 0.7f, 0f),
                end = Offset(width, height * 0.25f)
            ),
            style = Fill
        )

        // Bottom-left decorative blob
        val bottomBlobPath = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.15f, height * 0.75f,
                width * 0.2f, height * 0.9f,
                width * 0.35f, height
            )
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = bottomBlobPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    blendedSecondary.copy(alpha = 0.12f),
                    blendedPrimary.copy(alpha = 0.08f)
                ),
                start = Offset(0f, height * 0.7f),
                end = Offset(width * 0.35f, height)
            ),
            style = Fill
        )

        // Subtle circles
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendedPrimary.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(width * 0.1f, height * 0.3f),
                radius = width * 0.3f
            ),
            radius = width * 0.3f,
            center = Offset(width * 0.1f, height * 0.3f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendedSecondary.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(width * 0.9f, height * 0.7f),
                radius = width * 0.25f
            ),
            radius = width * 0.25f,
            center = Offset(width * 0.9f, height * 0.7f)
        )
    }
}

private fun lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
