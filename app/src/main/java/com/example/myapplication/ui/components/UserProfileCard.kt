package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.*
import com.example.myapplication.ui.theme.AnimationSpecs
import com.example.myapplication.ui.theme.FastOutSlowInEasing
import com.example.myapplication.ui.theme.AppIcons
import com.example.myapplication.ui.viewmodel.TodoViewModel
import com.example.myapplication.data.models.getXpRequirementsForLevels

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    // Remember previous values to detect changes for animations
    var prevLevel by remember { mutableStateOf(userProfile.level) }
    var prevXp by remember { mutableStateOf(userProfile.totalXp) }
    
    // Animate when XP or level changes
    val isLevelUp = userProfile.level > prevLevel
    val xpChanged = userProfile.totalXp > prevXp
    
    // Update previous values after detecting changes
    LaunchedEffect(userProfile.level, userProfile.totalXp) {
        prevLevel = userProfile.level
        prevXp = userProfile.totalXp
    }
    
    // Animate the card scale on level up
    val cardScale by animateFloatAsState(
        targetValue = if (isLevelUp) 1.03f else 1f,
        animationSpec = AnimationSpecs.SpringBouncy,
        label = "cardScale"
    )
    
    // Continuous subtle floating animation for the card
    FloatingElement(
        floatHeight = 5f,
        floatDuration = 3000
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .scale(cardScale),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Add rotating star icon before the title
                    PulsatingIcon(
                        icon = Icons.Default.Star,
                        contentDescription = "User Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        rotateEnabled = true,
                        pulseEnabled = true,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Animate level text when level changes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    // Animated level display
                    AnimatedContent(
                        targetState = userProfile.level,
                        transitionSpec = {
                            if (targetState > initialState) {
                                // Level up animation
                                (slideInVertically { height -> height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                            } else {
                                // Level down animation (rare but possible)
                                (slideInVertically { height -> -height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
                            }
                        },
                        label = "levelAnimation"
                    ) { level ->
                        Text(
                            text = "$level",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total XP:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    // Animate XP changes
                    AnimatedContent(
                        targetState = userProfile.totalXp,
                        transitionSpec = {
                            if (targetState > initialState) {
                                // XP increase animation
                                (slideInVertically { height -> height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                            } else {
                                // XP decrease animation (rare)
                                (slideInVertically { height -> -height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
                            }
                        },
                        label = "xpAnimation"
                    ) { xp ->
                        Text(
                            text = "$xp",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Add pulsating icon (but not rotating) for XP progress
                    PulsatingIcon(
                        icon = AppIcons.getRankIcon(getCategoryRankLevel(userProfile.level)),
                        contentDescription = "XP Progress",
                        tint = MaterialTheme.colorScheme.primary,
                        rotateEnabled = false,
                        pulseEnabled = true,
                        pulseMinScale = 0.9f,
                        pulseMaxScale = 1.1f,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "XP Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Animate progress bar
                val progress = calculateProgressToNextLevel(userProfile.totalXp)
                
                // Animate progress when XP changes
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = AnimationSpecs.TweenSlow,
                    label = "progressAnimation"
                )
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                
                // XP needed for next level
                val currentLevel = userProfile.level
                val xpForNextLevel = calculateXpForNextLevel(userProfile.totalXp)
                val xpNeeded = xpForNextLevel - userProfile.totalXp
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$xpNeeded XP to Level ${currentLevel + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Show information about XP needed for next level
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "XP to next level: ${xpForNextLevel - userProfile.totalXp}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
} 