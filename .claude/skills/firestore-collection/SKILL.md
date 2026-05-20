Scaffold a new Firestore collection integration. Usage: `/firestore-collection <collection-name> --ops <read|write|readwrite>`

1. Run the scaffold script:
   ```bash
   bash .claude/skills/firestore-collection/scaffold.sh <collection-name> --ops <read|write|readwrite>
   ```
2. Show the user the list of generated files.
3. Open the generated `<Collection>Record.kt` data class and ask the user to confirm the field names and types that match the actual Firestore document schema before proceeding.
4. Remind the user to add `@Binds` entries for the new repository and datasource in `data/src/main/.../data/di/DataModule.kt`.
5. Remind the user to update `docs/TECH_SPEC.md` Section 15 with the new collection schema.

Do not fill in the data class fields without the user confirming the schema.
