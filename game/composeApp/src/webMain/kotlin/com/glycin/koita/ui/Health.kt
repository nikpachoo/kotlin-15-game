package com.glycin.koita.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glycin.koita.composables.isCompact
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.heart
import org.jetbrains.compose.resources.painterResource

@Composable
fun Health(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier,
) {
    val compact = isCompact()
    val heartSize = if (compact) 22.dp else 32.dp
    val gap = if (compact) 2.dp else 4.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        val heartsPerRow = 9
        val numRows = (maxHp + heartsPerRow - 1) / heartsPerRow

        repeat(numRows) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                val startIndex = rowIndex * heartsPerRow
                val endIndex = minOf(startIndex + heartsPerRow, maxHp)

                repeat(endIndex - startIndex) { colIndex ->
                    val heartIndex = startIndex + colIndex
                    Image(
                        painter = painterResource(Res.drawable.heart),
                        contentDescription = "Heart",
                        modifier = Modifier.size(heartSize),
                        alpha = if (heartIndex < currentHp) 1f else 0.3f
                    )
                }
            }
        }
    }
}
