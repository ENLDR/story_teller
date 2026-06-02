package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.NarrativeElement
import com.example.data.HorrorStory
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorytellerApp(viewModel: HorrorStoryViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Create, 1: Listening, 2: Anthology

    val allStories by viewModel.allStories.collectAsStateWithLifecycle()
    val favoriteStories by viewModel.favoriteStories.collectAsStateWithLifecycle()
    val genState by viewModel.generationState.collectAsStateWithLifecycle()

    val currentStory by viewModel.currentPlayingStory.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val parsedElements by viewModel.parsedElements.collectAsStateWithLifecycle()
    val currentElementIndex by viewModel.currentElementIndex.collectAsStateWithLifecycle()
    val activeBgm by viewModel.activeBgm.collectAsStateWithLifecycle()
    val activeSfx by viewModel.activeSfx.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground),
        topBar = {
            if (activeTab != 1 || currentStory == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBackground)
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "භීතිකා",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "Sinhala Horror Storyteller & Audio Mixer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ShadowGrey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Visual Pulse Separation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, CrimsonPrimary, Color.Transparent)
                                )
                            )
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianBackground,
                tonalElevation = 8.dp,
                modifier = Modifier.border(0.5.dp, DarkCrimsonSurface, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
                    label = { Text("මවන්න (Create)") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ScarletAccent,
                        selectedTextColor = ScarletAccent,
                        unselectedIconColor = ShadowGrey,
                        unselectedTextColor = ShadowGrey,
                        indicatorColor = DarkCrimsonSurface
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { 
                        BadgedBox(badge = {
                            if (isPlaying) {
                                Badge(containerColor = ScarletAccent) {
                                    Text("🔊", color = GhostlyWhite, fontSize = 8.sp)
                                }
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Audience Room") 
                        }
                    },
                    label = { Text("සවන්දෙන්න (View)") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ScarletAccent,
                        selectedTextColor = ScarletAccent,
                        unselectedIconColor = ShadowGrey,
                        unselectedTextColor = ShadowGrey,
                        indicatorColor = DarkCrimsonSurface
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Anthology") },
                    label = { Text("පුස්තකාලය (Library)") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ScarletAccent,
                        selectedTextColor = ScarletAccent,
                        unselectedIconColor = ShadowGrey,
                        unselectedTextColor = ShadowGrey,
                        indicatorColor = DarkCrimsonSurface
                    )
                )
            }
        },
        containerColor = ObsidianBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> CreatorTab(viewModel = viewModel, onNavigateToPlayer = { activeTab = 1 })
                1 -> ListenerTab(viewModel = viewModel)
                2 -> LibraryTab(viewModel = viewModel, onPlayStory = { story ->
                    viewModel.startStoryPlayback(story)
                    activeTab = 1
                })
            }
        }
    }
}

@Composable
fun CreatorTab(viewModel: HorrorStoryViewModel, onNavigateToPlayer: () -> Unit) {
    val context = LocalContext.current
    val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val length by viewModel.selectedLength.collectAsStateWithLifecycle()
    val customVal by viewModel.customElement.collectAsStateWithLifecycle()
    val suspense by viewModel.selectedSuspense.collectAsStateWithLifecycle()
    val genState by viewModel.generationState.collectAsStateWithLifecycle()

    val categories = listOf(
        "සොහොන් බිම (Graveyard)",
        "විනාශ වූ මන්දිරය (Ruined Mansion)",
        "මූසල වනාන්තරය (Dark Jungle)",
        "අවතාර දුම්රිය (Phantom Train)"
    )

    val lengths = listOf("කෙටි (Short Tale)", "මධ්‍යම (Medium Tale)")

    var creatorMode by remember { mutableStateOf(0) } // 0: AI Creator, 1: Custom Story Write
    var customTitle by remember { mutableStateOf("") }
    var customContent by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf(categories.first()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "භීතිකා නිර්මාතෘ (Horror Creator)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GhostlyWhite,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "අද්භූත සැකසුම් ඇතුළත් කර බියකරු කතාවක් සජීවීව නිර්මාණය කරන්න.",
                fontSize = 12.sp,
                color = ShadowGrey
            )
        }

        // Mode Navigation segmented buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x13FFFFFF))
                    .border(0.5.dp, Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (creatorMode == 0) CrimsonPrimary else Color.Transparent)
                        .clickable { creatorMode = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI නිර්මාතෘ (AI Creator)",
                        color = if (creatorMode == 0) GhostlyWhite else ShadowGrey,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (creatorMode == 1) CrimsonPrimary else Color.Transparent)
                        .clickable { creatorMode = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "මගේ නිර්මාණ (Custom)",
                        color = if (creatorMode == 1) GhostlyWhite else ShadowGrey,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (creatorMode == 0) {
            // AI Creator Sub-Form
            item {
                Text(
                    text = "ස්ථානය තෝරන්න (Select Location)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        val isSelected = category.contains(cat.split(" ").first())
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0x28B91C1C) else Color(0x3318181D))
                                .border(1.dp, if (isSelected) ScarletAccent else Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectCategory(cat) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(cat) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ScarletAccent,
                                    unselectedColor = ShadowGrey
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cat,
                                color = if (isSelected) GhostlyWhite else ShadowGrey,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Length Capsules
            item {
                Text(
                    text = "කතාවේ දිග (Story Length)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    lengths.forEach { len ->
                        val isSelected = length.contains(len.split(" ").first())
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) CrimsonPrimary else Color(0x3318181D))
                                .border(1.dp, if (isSelected) ScarletAccent else Color(0x0DFFFFFF), RoundedCornerShape(24.dp))
                                .clickable { viewModel.selectLength(len) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = len,
                                color = if (isSelected) GhostlyWhite else ShadowGrey,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Custom Terrifying Element
            item {
                Text(
                    text = "භීතියේ සංකේතය (Terrifying Element / Object)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = customVal,
                    onValueChange = { viewModel.setCustomElement(it) },
                    placeholder = { Text("උදා: හඬන බෝනික්කා, ජනේලයේ තට්ටු හඬ...", color = Color.Gray, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GhostlyWhite,
                        unfocusedTextColor = GhostlyWhite,
                        focusedBorderColor = ScarletAccent,
                        unfocusedBorderColor = DarkCrimsonSurface,
                        focusedContainerColor = DarkCrimsonSurface,
                        unfocusedContainerColor = DarkCrimsonSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_element_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Trigger Button and Status Overlay
            item {
                Spacer(modifier = Modifier.height(8.dp))

                when (genState) {
                    is GenerationState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCrimsonSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, CrimsonPrimary, RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ScarletAccent)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "අඳුරු බලවේග කැඳවමින් පවතී...",
                                    color = GhostlyWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Gemini is scripting Sinhala horrors format...",
                                    color = ShadowGrey,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is GenerationState.Success -> {
                        val story = (genState as GenerationState.Success).story
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCrimsonSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, BloodGold, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = "Success", tint = BloodGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "භීෂණය සාර්ථකව උත්පාදනය විය!",
                                        color = BloodGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "නිර්මාණය වූ කතාව: ${story.title}",
                                    color = GhostlyWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.resetGenerationState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkMutedCard),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("තව එකක් (New)")
                                    }
                                    Button(
                                        onClick = { 
                                            viewModel.startStoryPlayback(story)
                                            viewModel.resetGenerationState()
                                            onNavigateToPlayer()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Text("දැන් අසන්න (Listen)")
                                    }
                                }
                            }
                        }
                    }
                    is GenerationState.Error -> {
                        val err = (genState as GenerationState.Error).message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCrimsonSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Red, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "දෝෂයකි: ${err}",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "කරුණාකර Google AI Studio Secrets පුවරුවේ ඔබගේ GEMINI_API_KEY යතුර ඇතුළත් කර ඇතිදැයි පරීක්ෂා කරන්න.",
                                    color = ShadowGrey,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.resetGenerationState() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("නැවත උත්සාහ කරන්න (Retry)")
                                }
                            }
                        }
                    }
                    else -> {
                        // Action Trigger
                        Button(
                            onClick = { viewModel.generateStory() },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("submit_button"),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "භීෂණය අවදි කරන්න (Incite Terror)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GhostlyWhite
                            )
                        }
                    }
                }
            }
        } else {
            // Manual Writer Sub-Form (Custom entry by typing/pasting)
            item {
                Text(
                    text = "කතාවේ මාතෘකාව (Story Title)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    placeholder = { Text("උදා: සොහොන් ගැබක අභිරහස...", color = Color.Gray, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GhostlyWhite,
                        unfocusedTextColor = GhostlyWhite,
                        focusedBorderColor = ScarletAccent,
                        unfocusedBorderColor = DarkCrimsonSurface,
                        focusedContainerColor = DarkCrimsonSurface,
                        unfocusedContainerColor = DarkCrimsonSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Text(
                    text = "කාණ්ඩය / ස්ථානය (Select Category)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = customCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) CrimsonPrimary else Color(0x3318181D))
                                .border(1.dp, if (isSel) ScarletAccent else Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .clickable { customCategory = cat }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.split(" ").first(), // short display name
                                color = if (isSel) GhostlyWhite else ShadowGrey,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ශබ්ද ප්‍රයෝග ඇතුළත් කිරීම් (Embedded Sound FX)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "කතාව අසන විට සජීවීව ශබ්ද සහ සංගීතය වාදනය කිරීමට පහත ටැග් එකතු කරන්න:",
                    fontSize = 11.sp,
                    color = ShadowGrey,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val sfxTags = listOf(
                    "🎵 පසුබිම් සංගීතය" to "[BGM: Low Eerie Drone]\n",
                    "😱 කෑගැසීම" to " [SFX: Scream] ",
                    "🚪 දොර හඬ" to " [SFX: Creak] ",
                    "💓 හද ගැස්ම" to " [SFX: Heartbeat] ",
                    "🌬️ සුළඟ" to " [SFX: Wind] ",
                    "🌧️ වැස්ස" to " [SFX: Rain] ",
                    "👁️ කොඳුරීම" to " [SFX: Whisper] ",
                    "💥 බිඳීමක්" to " [SFX: Shatter] "
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sfxTags) { (label, insertionValue) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x28B91C1C))
                                .border(0.5.dp, Color(0x55B91C1C), RoundedCornerShape(16.dp))
                                .clickable { 
                                    customContent += insertionValue
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = ScarletAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "කතාවේ විස්තරය (Story Text in Sinhala)",
                    fontSize = 14.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = customContent,
                    onValueChange = { customContent = it },
                    placeholder = { 
                        Text(
                            text = "කතාව සිංහලෙන් ලියන්න...\n\n(උදා:\n[BGM: Low Eerie Drone]\nඒ පාලු රාත්‍රිය ඉතා සීතලයි... [SFX: Creak] දොර විවෘත වුණා... [SFX: Scream] ජනේලය අසල කවුදෝ සිටියා...)", 
                            color = Color.Gray, 
                            fontSize = 13.sp
                        ) 
                    },
                    minLines = 8,
                    maxLines = 15,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GhostlyWhite,
                        unfocusedTextColor = GhostlyWhite,
                        focusedBorderColor = ScarletAccent,
                        unfocusedBorderColor = DarkCrimsonSurface,
                        focusedContainerColor = DarkCrimsonSurface,
                        unfocusedContainerColor = DarkCrimsonSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        if (customContent.isBlank()) {
                            android.widget.Toast.makeText(context, "කරුණාකර කතාවේ අන්තර්ගතය ඇතුළත් කරන්න.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveCustomStory(
                                title = customTitle.ifBlank { "මගේ අභිරහස් කතාව" },
                                content = customContent,
                                category = customCategory,
                                onCompleted = { savedStory ->
                                    viewModel.startStoryPlayback(savedStory)
                                    onNavigateToPlayer()
                                    // Reset inputs
                                    customTitle = ""
                                    customContent = ""
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_button"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "සුරකින්න සහ සවන්දෙන්න (Save & Listen)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GhostlyWhite
                    )
                }
            }
        }
    }
}

@Composable
fun ListenerTab(viewModel: HorrorStoryViewModel) {
    val currentStory by viewModel.currentPlayingStory.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val elements by viewModel.parsedElements.collectAsStateWithLifecycle()
    val progressIndex by viewModel.currentElementIndex.collectAsStateWithLifecycle()
    val activeBgm by viewModel.activeBgm.collectAsStateWithLifecycle()
    val activeSfx by viewModel.activeSfx.collectAsStateWithLifecycle()

    val currentVoiceIndex by viewModel.currentVoiceIndex.collectAsStateWithLifecycle()
    val activeVoice = viewModel.voices.getOrNull(currentVoiceIndex) ?: viewModel.voices.first()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Keep scrolling list updated to focus on currently speaking Text line
    LaunchedEffect(progressIndex) {
        if (progressIndex in elements.indices) {
            val element = elements[progressIndex]
            if (element is NarrativeElement.Text) {
                // Find index inside LazyColumn
                val listIndex = progressIndex + 2 // header offset
                scope.launch {
                    listState.animateScrollToItem((listIndex - 2).coerceAtLeast(0))
                }
            }
        }
    }

    if (currentStory == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "No stories",
                tint = DarkCrimsonSurface,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "තවමත් කතාවක් තෝරා නොමැත",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GhostlyWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = "පුස්තකාලයෙන් කතාවක් තෝරන්න හෝ කථා නිර්මාතෘ වෙතින් අලුත් එකක් උත්පාදනය කරන්න.",
                fontSize = 12.sp,
                color = ShadowGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        return
    }

    // Outer visual radial glow background (simulating absolute inset bg-radial in Design HTML)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val size = this.size
                drawRect(color = ObsidianBackground) // draw master solid bg
                
                // Draw elegant crimson atmospheric radial glow centered on the top-middle player area
                val brush = Brush.radialGradient(
                    colors = listOf(CrimsonPrimary.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(x = size.width / 2f, y = size.height * 0.35f),
                    radius = size.width * 0.85f
                )
                drawRect(brush = brush)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Immersive Header matching Tailwind: px-6 pt-10 pb-6
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Back button: w-10 h-10, bg-red-950/30, border-red-900/40
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x19B91C1C)) // bg-[#3F0C0C]/30-ish simulation
                            .border(1.dp, Color(0x33B91C1C), RoundedCornerShape(20.dp))
                            .clickable { viewModel.stopPlayback() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            color = ScarletAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column {
                        Text(
                            text = "සජීවී විකාශය", // Live streaming
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                        Text(
                            text = currentStory?.title ?: "මධ්‍යම රාත්‍රිය",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GhostlyWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download Story Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x0FFFFFFF))
                            .clickable { viewModel.downloadStoryAsFile(currentStory!!) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Download story text",
                            tint = GhostlyWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Options/More vertical dots
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x0FFFFFFF))
                            .clickable { viewModel.cycleVoice() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⋮",
                            color = GhostlyWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Current location tag inline subtitle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = ScarletAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentStory?.category ?: "Unknown Location",
                    fontSize = 11.sp,
                    color = ShadowGrey,
                    fontWeight = FontWeight.Medium
                )
            }

            // Story narrative Card space: backdrop-blur glass container with white/5 borders
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x3318181D)) // zinc-900/40 translucent look
                    .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(28.dp))
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "--- සන්නිවේදනය ක්‍රියාත්මකයි (Audio Link Active) ---",
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            color = CrimsonPrimary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    items(elements.size) { idx ->
                        val elem = elements[idx]
                        val isCurrent = idx == progressIndex

                        when (elem) {
                            is NarrativeElement.Text -> {
                                val alpha = if (isCurrent) 1.0f else 0.35f
                                val sizeScale = if (isCurrent) 22.sp else 16.sp
                                val weight = if (isCurrent) FontWeight.Light else FontWeight.Light

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCurrent) Color(0x11FFFFFF) else Color.Transparent)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = elem.content,
                                        fontSize = sizeScale,
                                        fontWeight = weight,
                                        color = GhostlyWhite.copy(alpha = alpha),
                                        lineHeight = 32.sp,
                                        fontFamily = FontFamily.Serif,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            is NarrativeElement.SoundEffect -> {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0x3C1F1F22)) // bg-zinc-800/60
                                            .padding(vertical = 5.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🔊 [SFX: ${elem.effect.uppercase()}]",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ShadowGrey,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            is NarrativeElement.BackgroundMusic -> {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0x28B91C1C)) // bg-red-950/40 border-red-900/30
                                            .border(0.5.dp, Color(0x55B91C1C), RoundedCornerShape(16.dp))
                                            .padding(vertical = 5.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🎵 [BGM: ${elem.music.uppercase()}]",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ScarletAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "--- සම්පූර්ණයි (End of Narrative) ---",
                            fontSize = 9.sp,
                            color = CrimsonPrimary.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulse Active Oscilloscope (from Soundwave Design)
            SpookyVisualizer(
                isPlaying = isPlaying,
                activeBgm = activeBgm,
                activeSfx = activeSfx
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Immersive Timeline Progress Bar matching Tailwind: 04:12 --------- 12:45
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // Calculate virtual playback timer in MM:SS form
                val totalSteps = elements.size
                val currentStep = (progressIndex + 1).coerceIn(0, totalSteps)
                
                val totalSeconds = totalSteps * 15
                val currentSeconds = currentStep * 15
                
                val totalMin = totalSeconds / 60
                val totalSec = totalSeconds % 60
                val currentMin = currentSeconds / 60
                val currentSec = currentSeconds % 60
                
                val timeStartStr = String.format("%02d:%02d", currentMin, currentSec)
                val timeEndStr = String.format("%02d:%02d", totalMin, totalSec)
                
                val progressRatio = if (totalSteps > 0) currentStep.toFloat() / totalSteps.toFloat() else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStartStr,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Custom linear seeking progress bar with seek thumb
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp)
                            .height(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Background trace
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF212124))
                        )
                        
                        // Active progress trace with subtle glow red
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(CrimsonPrimary, ScarletAccent)
                                    )
                                )
                        )
                        
                        // Progress head thumb circle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(GhostlyWhite)
                            )
                        }
                    }

                    Text(
                        text = timeEndStr,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ShadowGrey,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Immersive Media Button panel controls matching HTML design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Repeat / Restart (↺)
                IconButton(
                    onClick = { viewModel.restartPlayback() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        text = "↺",
                        color = ShadowGrey,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 2: Skip previous segment (⏮)
                IconButton(
                    onClick = { viewModel.previousElement() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        text = "⏮",
                        color = if (progressIndex > 0) GhostlyWhite else ShadowGrey.copy(alpha = 0.3f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 3: Main center Elevated Crimson Pulsating Play/Pause Sphere (w-20 h-20 bg-red-700)
                val playPulseAnim = rememberInfiniteTransition()
                val scale by playPulseAnim.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (isPlaying) 1.08f else 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .size((74.dp.value * scale).dp)
                        .clip(RoundedCornerShape(37.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(ScarletAccent, CrimsonPrimary)
                            )
                        )
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("submit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        color = GhostlyWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 4: Skip next segment (⏭)
                IconButton(
                    onClick = { viewModel.nextElement() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        text = "⏭",
                        color = if (progressIndex < elements.size - 1) GhostlyWhite else ShadowGrey.copy(alpha = 0.3f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 5: Loop state or cycle voice config (↻)
                IconButton(
                    onClick = { viewModel.cycleVoice() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        text = "↻",
                        color = ShadowGrey,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Spooky Voice Engine Info Card Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x3B18181C)) // bg-zinc-900/60
                    .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x19FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎙️",
                            fontSize = 20.sp
                        )
                    }
                    Column {
                        Text(
                            text = "VOICE ENGINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShadowGrey,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activeVoice.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GhostlyWhite
                        )
                    }
                }

                Button(
                    onClick = { viewModel.cycleVoice() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x0FFFFFFF),
                        contentColor = GhostlyWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color(0x22FFFFFF)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "මාරු කරන්න", // Change
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SpookyVisualizer(isPlaying: Boolean, activeBgm: String, activeSfx: String) {
    val waveTransition = rememberInfiniteTransition()
    val phase by waveTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCrimsonSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val points = 150
            val path = Path()

            path.moveTo(0f, height / 2f)

            for (i in 0..points) {
                val x = (i.toFloat() / points) * width
                val ratio = sin(i.toFloat() / points * Math.PI).toFloat() // fade wave at edges
                
                // Adjust frequency and amplitude based on player state
                val amp = if (!isPlaying) {
                    2f
                } else if (activeSfx.isNotEmpty()) {
                    35f * (Math.random().toFloat() * 0.4f + 0.8f) // spikes
                } else {
                    12f + 8f * sin(2.5f * i.toFloat() / points + phase) // slow hum
                }

                val freq = if (activeSfx.isNotEmpty()) {
                    0.25f
                } else if (isPlaying) {
                    0.09f
                } else {
                    0.02f
                }

                val y = (height / 2f) + sin(i * freq + phase) * amp * ratio
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = if (activeSfx.isNotEmpty()) ScarletAccent else if (isPlaying) CrimsonPrimary else ShadowGrey.copy(0.4f),
                style = Stroke(width = if (activeSfx.isNotEmpty()) 4f else 2.5f)
            )
        }

        // Active parameters texts overlays
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = activeBgm,
                    fontSize = 11.sp,
                    color = BloodGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (activeSfx.isNotEmpty()) {
                Text(
                    text = activeSfx,
                    fontSize = 11.sp,
                    color = ScarletAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(ScarletAccent.copy(0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun LibraryTab(
    viewModel: HorrorStoryViewModel,
    onPlayStory: (HorrorStory) -> Unit
) {
    val allStories by viewModel.allStories.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "භීෂණ මංජුසාව (The Anthology Catalog)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GhostlyWhite
                )
                Text(
                    text = "ඔබ විසින් තෝරාගත් හෝ නිර්මාණය කළ සියලු කතා.",
                    fontSize = 12.sp,
                    color = ShadowGrey
                )
            }
        }

        if (allStories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "කථා කිසිවක් නැත. 'මවන්න' වෙතින් නව කතාවක් ජනිත කරන්න.",
                    color = ShadowGrey,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(allStories, key = { it.id }) { story ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x3318181D))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GhostlyWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ස්ථානය: ${story.category}",
                                fontSize = 11.sp,
                                color = ShadowGrey
                            )
                        }
                    }

                    // Favorites Trigger Icon
                    IconButton(
                        onClick = { viewModel.toggleFavorite(story) }
                    ) {
                        Icon(
                            imageVector = if (story.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (story.isFavorite) CrimsonPrimary else ShadowGrey
                        )
                    }

                    // Play Trigger Icon
                    IconButton(
                        onClick = { onPlayStory(story) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(CrimsonPrimary.copy(0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = ScarletAccent
                        )
                    }

                    // Download Story Icon
                    IconButton(
                        onClick = { viewModel.downloadStoryAsFile(story) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Download story text",
                            tint = ShadowGrey
                        )
                    }

                    // Delete Trigger Icon (for non-default stories or optionally all)
                    IconButton(
                        onClick = { viewModel.deleteStory(story) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ShadowGrey
                        )
                    }
                }
            }
        }
    }
}
