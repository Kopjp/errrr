package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OilGold
import com.example.ui.theme.OilGoldContainer
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OlivePrimaryContainer
import com.example.ui.theme.OliveSecondary
import com.example.ui.theme.OliveSecondaryContainer
import com.example.ui.viewmodel.PressViewModel
import com.example.ui.viewmodel.StockSummary

@Composable
fun StockSummaryCards(
    stock: StockSummary,
    viewModel: PressViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_summary_cards_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: الزيتون وزيت الزيتون
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StockMetricCard(
                title = "مخزون الزيتون",
                mainValue = "${viewModel.formatNumber(stock.olivesStockKg)} كغ",
                subValue = if (stock.olivesStockKg >= 1000) "≈ ${viewModel.formatNumber(stock.olivesStockKg / 1000.0, 2)} طن" else "جاهز للعصر",
                icon = Icons.Default.Agriculture,
                containerColor = OlivePrimaryContainer.copy(alpha = 0.5f),
                iconColor = OlivePrimary,
                modifier = Modifier.weight(1f)
            )

            StockMetricCard(
                title = "مخزون زيت الزيتون",
                mainValue = "${viewModel.formatNumber(stock.oilStockKg)} كغ",
                subValue = "≈ ${viewModel.formatNumber(stock.oilStockTins, 1)} تنكة",
                icon = Icons.Default.WaterDrop,
                containerColor = OilGoldContainer.copy(alpha = 0.5f),
                iconColor = OilGold,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: التنكات الفارغة وتفل الزيتون (البيرين)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StockMetricCard(
                title = "التنكات الفارغة",
                mainValue = "${stock.emptyTinsCount} تنكة",
                subValue = "مخزون التعبئة المتاح",
                icon = Icons.Default.Archive,
                containerColor = OliveSecondaryContainer.copy(alpha = 0.45f),
                iconColor = OliveSecondary,
                modifier = Modifier.weight(1f)
            )

            StockMetricCard(
                title = "تفل الزيتون (جفت)",
                mainValue = "${viewModel.formatNumber(stock.pomaceStockKg)} كغ",
                subValue = if (stock.pomaceStockKg >= 1000) "≈ ${viewModel.formatNumber(stock.pomaceStockKg / 1000.0, 2)} طن" else "متاح للبيع كوقود",
                icon = Icons.Default.Scale,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        // Storage Tanks Capacity Indicator
        if (stock.tanksTotalCapacityKg > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = OlivePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إجمالي تخزين الخزانات المعدنية",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val percentage = (stock.tanksTotalStoredKg / stock.tanksTotalCapacityKg * 100).coerceIn(0.0, 100.0)
                        Text(
                            text = "${viewModel.formatNumber(percentage, 0)} % ممتلئ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OilGold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val fillRatio = (stock.tanksTotalStoredKg / stock.tanksTotalCapacityKg).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { fillRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OilGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المخزن حالياً: ${viewModel.formatNumber(stock.tanksTotalStoredKg)} كغ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "السعة القصوى: ${viewModel.formatNumber(stock.tanksTotalCapacityKg)} كغ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockMetricCard(
    title: String,
    mainValue: String,
    subValue: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = mainValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
