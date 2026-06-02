import 'package:flutter_test/flutter_test.dart';

import 'package:story_teller/audio/narrative_element.dart';

void main() {
  test('NarrativeParser splits text and BGM/SFX cues in order', () {
    const raw =
        '[BGM: Low Eerie Drone]\nඅඳුර වැඩි විය... [SFX: Door Creaking] දොර ඇරුණා...';
    final elements = NarrativeParser.parse(raw);

    expect(elements.length, 4);
    expect(elements[0], isA<BackgroundMusicElement>());
    expect((elements[0] as BackgroundMusicElement).music, 'Low Eerie Drone');
    expect(elements[1], isA<TextElement>());
    expect((elements[1] as TextElement).content, 'අඳුර වැඩි විය...');
    expect(elements[2], isA<SoundEffectElement>());
    expect((elements[2] as SoundEffectElement).effect, 'Door Creaking');
    expect(elements[3], isA<TextElement>());
  });

  test('NarrativeParser keeps trailing text after the last cue', () {
    final elements = NarrativeParser.parse('[SFX: Scream] කෑගැසීම...');
    expect(elements.first, isA<SoundEffectElement>());
    expect(elements.last, isA<TextElement>());
  });
}
