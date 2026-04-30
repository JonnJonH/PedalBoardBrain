package com.pedalboard.recreator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pedalboard.recreator.data.ChainStage
import com.pedalboard.recreator.data.ChannelType
import com.pedalboard.recreator.data.PedalEntity

@Composable
fun SignalChainDiagram(
    pedals: List<PedalEntity>,
    onPedalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val guitar     = pedals.find { it.chainStage == ChainStage.GUITAR }
    val amp        = pedals.find { it.chainStage == ChainStage.AMP }
    val preAmp     = pedals.filter { it.chainStage == ChainStage.PRE_AMP }.sortedBy { it.position }
    val fxLoop     = pedals.filter { it.chainStage == ChainStage.FX_LOOP }.sortedBy { it.position }

    val leftPedals  = fxLoop.filter { it.channel == ChannelType.LEFT  }.sortedBy { it.position }
    val rightPedals = fxLoop.filter { it.channel == ChannelType.RIGHT }.sortedBy { it.position }
    val singlePath  = fxLoop.filter { it.channel == ChannelType.MONO || it.channel == ChannelType.STEREO }.sortedBy { it.position }

    val hasSplit = leftPedals.isNotEmpty() || rightPedals.isNotEmpty()

    val splitStartPos = if (hasSplit) minOf(
        leftPedals.minByOrNull  { it.position }?.position ?: Int.MAX_VALUE,
        rightPedals.minByOrNull { it.position }?.position ?: Int.MAX_VALUE
    ) else Int.MAX_VALUE

    val splitEndPos = if (hasSplit) maxOf(
        leftPedals.maxByOrNull  { it.position }?.position ?: Int.MIN_VALUE,
        rightPedals.maxByOrNull { it.position }?.position ?: Int.MIN_VALUE
    ) else Int.MIN_VALUE

    val preSplitSingle  = singlePath.filter { it.position < splitStartPos }
    val postSplitSingle = singlePath.filter { it.position > splitEndPos }

    val splitRows: List<Pair<PedalEntity?, PedalEntity?>> = if (hasSplit) {
        (splitStartPos..splitEndPos).map { pos ->
            leftPedals.find { it.position == pos } to rightPedals.find { it.position == pos }
        }
    } else emptyList()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp)
    ) {
                // -- Guitar entry point ------------------------------------------------
        item {
            if (guitar != null) {
                NodeCard(guitar, onPedalClick, label = "GUITAR")
            } else {
                ChainTerminal("GUITAR IN")
            }
            DiagramArrow()
        }

        // -- Pre-amp pedals ----------------------------------------------------
        itemsIndexed(preAmp) { _, p ->
            NodeCard(p, onPedalClick)
            DiagramArrow()
        }

        // -- Amp (between pre-amp and FX loop) ---------------------------------
        item {
            amp?.let {
                NodeCard(it, onPedalClick, label = "AMP")
                DiagramArrow()
            }
        }

        // -- FX loop pre-split single path -------------------------------------
        itemsIndexed(preSplitSingle) { _, p ->
            NodeCard(p, onPedalClick, isStereo = p.channel == ChannelType.STEREO)
            DiagramArrow()
        }

        // -- Split rows (asymmetric branches with pass-through slots) ----------
        itemsIndexed(splitRows) { index, (left, right) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (index == 0) {
                        Text("LEFT", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                    }
                    if (left != null) NodeCard(left, onPedalClick) else PassThroughSlot()
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (index == 0) {
                        Text("RIGHT", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                    }
                    if (right != null) NodeCard(right, onPedalClick) else PassThroughSlot()
                }
            }
            DiagramArrow()
        }

        // -- Post-split / post-merge single path -------------------------------
        itemsIndexed(postSplitSingle) { _, p ->
            NodeCard(p, onPedalClick, isStereo = p.channel == ChannelType.STEREO)
            DiagramArrow()
        }

        // -- Amp return terminal -----------------------------------------------
        item {
            ChainTerminal("AMP RETURN")
        }
    }
}

@Composable
fun ChainTerminal(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
    }
}

/** Dashed vertical line - signal passing through unchanged on this side */
@Composable
fun PassThroughSlot() {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 100.dp)
            .drawBehind {
                val x = size.width / 2f
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            }
    )
}

@Composable
fun NodeCard(
    pedal: PedalEntity,
    onPedalClick: (String) -> Unit,
    isStereo: Boolean = false,
    label: String? = null
) {
    Card(
        onClick = { onPedalClick(pedal.id) },
        modifier = Modifier.size(width = 140.dp, height = 100.dp).padding(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (pedal.imagePath != null) {
                    AsyncImage(
                        model = pedal.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                // Stage badge (AMP, STEREO, etc.)
                val badge = label ?: if (isStereo) "STEREO" else null
                if (badge != null) {
                    Text(
                        badge,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        color = if (label != null) MaterialTheme.colorScheme.primary else Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                pedal.name,
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}

@Composable
fun DiagramArrow() {
    Icon(
        Icons.Default.ArrowDownward,
        contentDescription = null,
        modifier = Modifier.size(28.dp).padding(vertical = 2.dp),
        tint = Color.Gray.copy(alpha = 0.6f)
    )
}
