/// Sealed-class equivalent for a parsed narrative element.
sealed class NarrativeElement {
  const NarrativeElement();
}

class TextElement extends NarrativeElement {
  final String content;
  const TextElement(this.content);
}

class SoundEffectElement extends NarrativeElement {
  final String effect;
  const SoundEffectElement(this.effect);
}

class BackgroundMusicElement extends NarrativeElement {
  final String music;
  const BackgroundMusicElement(this.music);
}

/// Parses stories containing bracketed sound markers like
/// `[BGM: Low Eerie Drone]` or `[SFX: Door Creaking]`.
class NarrativeParser {
  static final RegExp _regex = RegExp(r'\[(BGM|SFX):\s*([^\]]+)\]');

  static List<NarrativeElement> parse(String rawText) {
    final elements = <NarrativeElement>[];
    var lastIndex = 0;

    for (final match in _regex.allMatches(rawText)) {
      final textBefore = rawText.substring(lastIndex, match.start).trim();
      if (textBefore.isNotEmpty) {
        elements.add(TextElement(textBefore));
      }

      final type = match.group(1)!;
      final value = match.group(2)!.trim();

      if (type.toUpperCase() == 'BGM') {
        elements.add(BackgroundMusicElement(value));
      } else if (type.toUpperCase() == 'SFX') {
        elements.add(SoundEffectElement(value));
      }

      lastIndex = match.end;
    }

    final textAfter = rawText.substring(lastIndex).trim();
    if (textAfter.isNotEmpty) {
      elements.add(TextElement(textAfter));
    }

    return elements;
  }
}
