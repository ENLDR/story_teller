import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config.dart';

/// Thin REST client for the Gemini `generateContent` endpoint,
/// replacing the Retrofit/Moshi `GeminiApiService`.
class GeminiService {
  static const String _baseUrl = 'https://generativelanguage.googleapis.com';

  final http.Client _client;

  GeminiService({http.Client? client}) : _client = client ?? http.Client();

  /// Returns the first text part of the first candidate.
  Future<String> generateContent({
    required String prompt,
    required String systemInstruction,
    double temperature = 0.85,
    int maxOutputTokens = 2000,
  }) async {
    final uri = Uri.parse(
      '$_baseUrl/v1beta/models/${AppConfig.geminiModel}:generateContent'
      '?key=${AppConfig.geminiApiKey}',
    );

    final body = jsonEncode({
      'contents': [
        {
          'parts': [
            {'text': prompt},
          ],
        },
      ],
      'systemInstruction': {
        'parts': [
          {'text': systemInstruction},
        ],
      },
      'generationConfig': {
        'temperature': temperature,
        'maxOutputTokens': maxOutputTokens,
      },
    });

    final response = await _client
        .post(
          uri,
          headers: {'Content-Type': 'application/json'},
          body: body,
        )
        .timeout(const Duration(seconds: 60));

    if (response.statusCode != 200) {
      throw Exception(
        'Gemini API error ${response.statusCode}: ${response.body}',
      );
    }

    final decoded = jsonDecode(response.body) as Map<String, dynamic>;
    final text = _extractText(decoded);
    if (text == null || text.isEmpty) {
      throw Exception('Received empty content from Gemini API.');
    }
    return text;
  }

  String? _extractText(Map<String, dynamic> decoded) {
    final candidates = decoded['candidates'];
    if (candidates is! List || candidates.isEmpty) return null;

    final content = (candidates.first as Map<String, dynamic>)['content'];
    if (content is! Map<String, dynamic>) return null;

    final parts = content['parts'];
    if (parts is! List || parts.isEmpty) return null;

    return (parts.first as Map<String, dynamic>)['text'] as String?;
  }
}
