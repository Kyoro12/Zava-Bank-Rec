package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ZavaCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, shape)
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ZavaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isGold: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "zava_button"
) {
    val containerColor = if (isGold) GoldAccent else MaterialTheme.colorScheme.primary
    val contentColor = if (isGold) NavyDeep else MaterialTheme.colorScheme.onPrimary

    Button(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ZavaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "zava_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = borderColor
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onActionClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CategoryIcon(
    category: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getCategoryIconAndColor(category, isIncome)
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    }
}

fun getCategoryIconAndColor(category: String, isIncome: Boolean): Pair<ImageVector, Color> {
    return if (isIncome) {
        when (category.lowercase()) {
            "gaji" -> Icons.Default.AccountBalance to SuccessGreen
            "bonus" -> Icons.Default.CardGiftcard to GoldAccent
            "bisnis" -> Icons.Default.Store to InfoBlue
            "freelance" -> Icons.Default.Laptop to PurpleLuxuryAccent
            "hadiah" -> Icons.Default.Celebration to GoldLight
            else -> Icons.Default.TrendingUp to SuccessGreen
        }
    } else {
        when (category.lowercase()) {
            "makanan" -> Icons.Default.Restaurant to Color(0xFFF97316)
            "transportasi" -> Icons.Default.DirectionsCar to Color(0xFF06B6D4)
            "belanja" -> Icons.Default.ShoppingCart to Color(0xFFEC4899)
            "kebutuhan pribadi" -> Icons.Default.Person to Color(0xFF8B5CF6)
            "tagihan" -> Icons.Default.ReceiptLong to Color(0xFFEF4444)
            "pendidikan" -> Icons.Default.School to Color(0xFF3B82F6)
            "hiburan" -> Icons.Default.SportsEsports to Color(0xFFA855F7)
            "kesehatan" -> Icons.Default.LocalHospital to Color(0xFF10B981)
            "pernikahan" -> Icons.Default.Favorite to Color(0xFFE5B80B)
            else -> Icons.Default.Receipt to Color(0xFF94A3B8)
        }
    }
}

@Composable
fun SimpleBarChart(
    income: Long,
    expense: Long,
    currencyFormat: String,
    modifier: Modifier = Modifier
) {
    val total = maxOf(1L, maxOf(income, expense))
    val incomeFraction = (income.toFloat() / total.toFloat()).coerceIn(0.05f, 1f)
    val expenseFraction = (expense.toFloat() / total.toFloat()).coerceIn(0.05f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp)
    ) {
        Text(
            text = "Perbandingan Arus Kas Bulan Ini",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Income Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(55.dp),
                color = SuccessGreen
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.Gray.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(incomeFraction)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            Brush.horizontalGradient(listOf(SuccessGreen, Color(0xFF34D399)))
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = FormatUtil.formatRupiah(income, currencyFormat),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Expense Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keluar",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(55.dp),
                color = DangerRed
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.Gray.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(expenseFraction)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            Brush.horizontalGradient(listOf(DangerRed, Color(0xFFF87171)))
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = FormatUtil.formatRupiah(expense, currencyFormat),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DangerRed
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
