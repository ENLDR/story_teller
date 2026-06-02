import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'state/horror_story_view_model.dart';
import 'theme/app_colors.dart';
import 'ui/storyteller_app.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const HorrorApp());
}

class HorrorApp extends StatelessWidget {
  const HorrorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => HorrorStoryViewModel(),
      child: MaterialApp(
        title: 'භීතිකා',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          useMaterial3: true,
          brightness: Brightness.dark,
          scaffoldBackgroundColor: AppColors.obsidianBackground,
          colorScheme: ColorScheme.fromSeed(
            seedColor: AppColors.crimsonPrimary,
            brightness: Brightness.dark,
            primary: AppColors.crimsonPrimary,
            secondary: AppColors.scarletAccent,
            surface: AppColors.darkCrimsonSurface,
          ),
          fontFamily: 'serif',
        ),
        home: const StorytellerApp(),
      ),
    );
  }
}
