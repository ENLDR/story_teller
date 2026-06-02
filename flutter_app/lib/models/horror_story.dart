/// Ported from the Room `HorrorStory` entity.
class HorrorStory {
  final int id;
  final String title;
  final String content;
  final String category;
  final int timestamp;
  final bool isFavorite;
  final String systemPromptUsed;

  HorrorStory({
    this.id = 0,
    required this.title,
    required this.content,
    required this.category,
    int? timestamp,
    this.isFavorite = false,
    this.systemPromptUsed = '',
  }) : timestamp = timestamp ?? DateTime.now().millisecondsSinceEpoch;

  HorrorStory copyWith({
    int? id,
    String? title,
    String? content,
    String? category,
    int? timestamp,
    bool? isFavorite,
    String? systemPromptUsed,
  }) {
    return HorrorStory(
      id: id ?? this.id,
      title: title ?? this.title,
      content: content ?? this.content,
      category: category ?? this.category,
      timestamp: timestamp ?? this.timestamp,
      isFavorite: isFavorite ?? this.isFavorite,
      systemPromptUsed: systemPromptUsed ?? this.systemPromptUsed,
    );
  }

  Map<String, Object?> toMap() {
    return {
      if (id != 0) 'id': id,
      'title': title,
      'content': content,
      'category': category,
      'timestamp': timestamp,
      'isFavorite': isFavorite ? 1 : 0,
      'systemPromptUsed': systemPromptUsed,
    };
  }

  factory HorrorStory.fromMap(Map<String, Object?> map) {
    return HorrorStory(
      id: (map['id'] as int?) ?? 0,
      title: map['title'] as String? ?? '',
      content: map['content'] as String? ?? '',
      category: map['category'] as String? ?? '',
      timestamp: map['timestamp'] as int?,
      isFavorite: (map['isFavorite'] as int? ?? 0) == 1,
      systemPromptUsed: map['systemPromptUsed'] as String? ?? '',
    );
  }
}
