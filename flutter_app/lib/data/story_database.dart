import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

import '../models/horror_story.dart';

/// sqflite-backed store, the Room `AppDatabase` + `StoryDao` equivalent.
class StoryDatabase {
  StoryDatabase._();
  static final StoryDatabase instance = StoryDatabase._();

  static const String _table = 'horror_stories';
  Database? _db;

  Future<Database> get database async {
    return _db ??= await _open();
  }

  Future<Database> _open() async {
    final dbPath = await getDatabasesPath();
    final path = p.join(dbPath, 'horror_stories_db.sqlite');
    return openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE $_table (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            category TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            isFavorite INTEGER NOT NULL DEFAULT 0,
            systemPromptUsed TEXT NOT NULL DEFAULT ''
          )
        ''');
      },
    );
  }

  Future<List<HorrorStory>> getAllStories() async {
    final db = await database;
    final rows = await db.query(_table, orderBy: 'timestamp DESC');
    return rows.map(HorrorStory.fromMap).toList();
  }

  Future<List<HorrorStory>> getFavoriteStories() async {
    final db = await database;
    final rows = await db.query(
      _table,
      where: 'isFavorite = 1',
      orderBy: 'timestamp DESC',
    );
    return rows.map(HorrorStory.fromMap).toList();
  }

  Future<HorrorStory?> getStoryById(int id) async {
    final db = await database;
    final rows = await db.query(_table, where: 'id = ?', whereArgs: [id]);
    if (rows.isEmpty) return null;
    return HorrorStory.fromMap(rows.first);
  }

  Future<int> insertStory(HorrorStory story) async {
    final db = await database;
    return db.insert(
      _table,
      story.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<void> updateStory(HorrorStory story) async {
    final db = await database;
    await db.update(
      _table,
      story.toMap(),
      where: 'id = ?',
      whereArgs: [story.id],
    );
  }

  Future<void> deleteStoryById(int id) async {
    final db = await database;
    await db.delete(_table, where: 'id = ?', whereArgs: [id]);
  }

  Future<void> deleteAllStories() async {
    final db = await database;
    await db.delete(_table);
  }
}
