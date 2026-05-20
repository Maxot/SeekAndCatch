Scaffold a new feature module. Usage: `/new-feature <feature-name>`

1. Run the scaffold script:
   ```bash
   bash .claude/skills/new-feature/scaffold.sh <feature-name>
   ```
2. Show the user the list of generated files.
3. Remind the user of the three manual wiring steps printed by the script:
   - Add `':feature:<name>'` to `settings.gradle.kts`
   - Add `implementation(project(':feature:<name>'))` to `app/build.gradle.kts`
   - Wire `<name>Screen()` into `SeekCatchNavHost` in `app/src/main/.../navigation/SeekCatchNavHost.kt`
4. Ask the user if they want you to perform these wiring steps now.

Do not modify `settings.gradle.kts`, `app/build.gradle.kts`, or `SeekCatchNavHost.kt` without confirmation.
