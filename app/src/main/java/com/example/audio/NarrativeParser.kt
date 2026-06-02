package com.example.audio

sealed class NarrativeElement {
    data class Text(val content: String) : NarrativeElement()
    data class SoundEffect(val effect: String) : NarrativeElement()
    data class BackgroundMusic(val music: String) : NarrativeElement()
}

object NarrativeParser {
    /**
     * Parses stories that contain bracketed sound markers like:
     * [BGM: Low Eerie Drone] or [SFX: Door Creaking]
     */
    fun parse(rawText: String): List<NarrativeElement> {
        val elements = mutableListOf<NarrativeElement>()
        val regex = Regex("\\[(BGM|SFX):\\s*([^\\]]+)\\]")
        var lastIndex = 0

        regex.findAll(rawText).forEach { matchResult ->
            // Extract the normal narrative text immediately before this marker
            val textBeforeRange = rawText.substring(lastIndex, matchResult.range.first).trim()
            if (textBeforeRange.isNotEmpty()) {
                elements.add(NarrativeElement.Text(textBeforeRange))
            }

            val type = matchResult.groupValues[1]
            val value = matchResult.groupValues[2].trim()

            if (type.equals("BGM", ignoreCase = true)) {
                elements.add(NarrativeElement.BackgroundMusic(value))
            } else if (type.equals("SFX", ignoreCase = true)) {
                elements.add(NarrativeElement.SoundEffect(value))
            }

            lastIndex = matchResult.range.last + 1
        }

        // Add any remaining text at the very end
        val textAfter = rawText.substring(lastIndex).trim()
        if (textAfter.isNotEmpty()) {
            elements.add(NarrativeElement.Text(textAfter))
        }

        return elements
    }
}
