#!/bin/bash
# Scaffolds a new Firestore collection integration.
# Usage: scaffold.sh <collection-name> --ops <read|write|readwrite>
# Example: scaffold.sh scores --ops readwrite

set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
BASE_PACKAGE="com.maxot.seekandcatch"
DATA_SRC="$REPO_ROOT/data/src/main/java/$(echo "${BASE_PACKAGE}.data" | tr '.' '/')"
COMMON_SRC="$REPO_ROOT/core/common/src/main/java/$(echo "${BASE_PACKAGE}.core.common.model" | tr '.' '/')"

if [[ $# -lt 3 ]]; then
  echo "Usage: scaffold.sh <collection-name> --ops <read|write|readwrite>"
  echo "  Example: scaffold.sh scores --ops readwrite"
  exit 1
fi

COLLECTION_RAW="$1"
shift

OPS=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --ops) OPS="$2"; shift 2 ;;
    *) echo "Unknown argument: $1"; exit 1 ;;
  esac
done

if [[ -z "$OPS" ]]; then
  echo "Error: --ops is required"
  exit 1
fi

COLLECTION_LOWER=$(echo "$COLLECTION_RAW" | tr '[:upper:]' '[:lower:]')
COLLECTION_CAP="$(echo "${COLLECTION_LOWER:0:1}" | tr '[:lower:]' '[:upper:]')${COLLECTION_LOWER:1}"

DO_READ=false
DO_WRITE=false
[[ "$OPS" == "read" || "$OPS" == "readwrite" ]] && DO_READ=true
[[ "$OPS" == "write" || "$OPS" == "readwrite" ]] && DO_WRITE=true

DATASOURCE_DIR="$DATA_SRC/firebase/datasource"
REPO_DIR="$DATA_SRC/repository"
mkdir -p "$DATASOURCE_DIR" "$REPO_DIR" "$COMMON_SRC"

echo "Scaffolding Firestore collection: $COLLECTION_CAP (ops: $OPS)"
echo ""

# ── Data class ────────────────────────────────────────────────
cat > "$COMMON_SRC/${COLLECTION_CAP}Record.kt" << EOF
package ${BASE_PACKAGE}.core.common.model

import com.google.firebase.firestore.DocumentId

data class ${COLLECTION_CAP}Record(
    @DocumentId val id: String = "",
    // Add fields matching Firestore document schema here
)
EOF

# ── DataSource interface ───────────────────────────────────────
DS_INTERFACE="$DATASOURCE_DIR/${COLLECTION_CAP}DataSource.kt"
{
  echo "package ${BASE_PACKAGE}.data.firebase.datasource"
  echo ""
  echo "import ${BASE_PACKAGE}.core.common.model.${COLLECTION_CAP}Record"
  if $DO_READ; then
    echo "import kotlinx.coroutines.flow.Flow"
  fi
  echo ""
  echo "interface ${COLLECTION_CAP}DataSource {"
  if $DO_READ; then
    echo "    fun observe${COLLECTION_CAP}Records(): Flow<List<${COLLECTION_CAP}Record>>"
    echo "    suspend fun get${COLLECTION_CAP}Records(): List<${COLLECTION_CAP}Record>"
  fi
  if $DO_WRITE; then
    echo "    suspend fun add${COLLECTION_CAP}Record(record: ${COLLECTION_CAP}Record)"
    echo "    suspend fun update${COLLECTION_CAP}Record(id: String, record: ${COLLECTION_CAP}Record)"
    echo "    suspend fun delete${COLLECTION_CAP}Record(id: String)"
  fi
  echo "}"
} > "$DS_INTERFACE"

# ── DataSource Firestore implementation ────────────────────────
DS_IMPL="$DATASOURCE_DIR/${COLLECTION_CAP}FirestoreDataSource.kt"
{
  echo "package ${BASE_PACKAGE}.data.firebase.datasource"
  echo ""
  echo "import android.util.Log"
  echo "import com.google.firebase.firestore.ktx.firestore"
  if $DO_READ; then
    echo "import com.google.firebase.firestore.snapshots"
    echo "import com.google.firebase.firestore.toObjects"
  fi
  echo "import com.google.firebase.ktx.Firebase"
  echo "import ${BASE_PACKAGE}.core.common.model.${COLLECTION_CAP}Record"
  if $DO_READ; then
    echo "import kotlinx.coroutines.flow.Flow"
    echo "import kotlinx.coroutines.flow.map"
  fi
  if $DO_WRITE; then
    echo "import kotlinx.coroutines.tasks.await"
  fi
  echo "import javax.inject.Inject"
  echo ""
  echo "private const val TAG = \"${COLLECTION_CAP}FirestoreDataSource\""
  echo "private const val COLLECTION_NAME = \"${COLLECTION_LOWER}\""
  echo ""
  echo "class ${COLLECTION_CAP}FirestoreDataSource @Inject constructor() : ${COLLECTION_CAP}DataSource {"
  echo ""
  echo "    private val db = Firebase.firestore"
  echo "    private val collection = db.collection(COLLECTION_NAME)"
  echo ""
  if $DO_READ; then
    echo "    override fun observe${COLLECTION_CAP}Records(): Flow<List<${COLLECTION_CAP}Record>> ="
    echo "        collection.snapshots().map { it.toObjects<${COLLECTION_CAP}Record>() }"
    echo ""
    echo "    override suspend fun get${COLLECTION_CAP}Records(): List<${COLLECTION_CAP}Record> = try {"
    echo "        collection.get().await().toObjects<${COLLECTION_CAP}Record>()"
    echo "    } catch (e: Exception) {"
    echo "        Log.w(TAG, \"Failed to get ${COLLECTION_LOWER} records\", e)"
    echo "        emptyList()"
    echo "    }"
    echo ""
  fi
  if $DO_WRITE; then
    echo "    override suspend fun add${COLLECTION_CAP}Record(record: ${COLLECTION_CAP}Record) {"
    echo "        try {"
    echo "            collection.add(record).await()"
    echo "        } catch (e: Exception) {"
    echo "            Log.w(TAG, \"Failed to add ${COLLECTION_LOWER} record\", e)"
    echo "        }"
    echo "    }"
    echo ""
    echo "    override suspend fun update${COLLECTION_CAP}Record(id: String, record: ${COLLECTION_CAP}Record) {"
    echo "        collection.document(id).set(record)"
    echo "            .addOnFailureListener { e -> Log.w(TAG, \"Failed to update ${COLLECTION_LOWER} record\", e) }"
    echo "    }"
    echo ""
    echo "    override suspend fun delete${COLLECTION_CAP}Record(id: String) {"
    echo "        try {"
    echo "            collection.document(id).delete().await()"
    echo "        } catch (e: Exception) {"
    echo "            Log.w(TAG, \"Failed to delete ${COLLECTION_LOWER} record\", e)"
    echo "        }"
    echo "    }"
    echo ""
  fi
  echo "}"
} > "$DS_IMPL"

# ── Repository interface ───────────────────────────────────────
REPO_IFACE="$REPO_DIR/${COLLECTION_CAP}Repository.kt"
{
  echo "package ${BASE_PACKAGE}.data.repository"
  echo ""
  echo "import ${BASE_PACKAGE}.core.common.model.${COLLECTION_CAP}Record"
  if $DO_READ; then
    echo "import kotlinx.coroutines.flow.Flow"
  fi
  echo ""
  echo "interface ${COLLECTION_CAP}Repository {"
  if $DO_READ; then
    echo "    fun observe${COLLECTION_CAP}Records(): Flow<List<${COLLECTION_CAP}Record>>"
    echo "    suspend fun get${COLLECTION_CAP}Records(): List<${COLLECTION_CAP}Record>"
  fi
  if $DO_WRITE; then
    echo "    suspend fun add${COLLECTION_CAP}Record(record: ${COLLECTION_CAP}Record)"
    echo "    suspend fun update${COLLECTION_CAP}Record(id: String, record: ${COLLECTION_CAP}Record)"
    echo "    suspend fun delete${COLLECTION_CAP}Record(id: String)"
  fi
  echo "}"
} > "$REPO_IFACE"

# ── Repository implementation ──────────────────────────────────
REPO_IMPL="$REPO_DIR/${COLLECTION_CAP}RepositoryImpl.kt"
{
  echo "package ${BASE_PACKAGE}.data.repository"
  echo ""
  echo "import ${BASE_PACKAGE}.core.common.model.${COLLECTION_CAP}Record"
  echo "import ${BASE_PACKAGE}.data.firebase.datasource.${COLLECTION_CAP}DataSource"
  if $DO_READ; then
    echo "import kotlinx.coroutines.flow.Flow"
  fi
  echo "import javax.inject.Inject"
  echo ""
  echo "class ${COLLECTION_CAP}RepositoryImpl @Inject constructor("
  echo "    private val dataSource: ${COLLECTION_CAP}DataSource"
  echo ") : ${COLLECTION_CAP}Repository {"
  echo ""
  if $DO_READ; then
    echo "    override fun observe${COLLECTION_CAP}Records(): Flow<List<${COLLECTION_CAP}Record>> ="
    echo "        dataSource.observe${COLLECTION_CAP}Records()"
    echo ""
    echo "    override suspend fun get${COLLECTION_CAP}Records(): List<${COLLECTION_CAP}Record> ="
    echo "        dataSource.get${COLLECTION_CAP}Records()"
    echo ""
  fi
  if $DO_WRITE; then
    echo "    override suspend fun add${COLLECTION_CAP}Record(record: ${COLLECTION_CAP}Record) ="
    echo "        dataSource.add${COLLECTION_CAP}Record(record)"
    echo ""
    echo "    override suspend fun update${COLLECTION_CAP}Record(id: String, record: ${COLLECTION_CAP}Record) ="
    echo "        dataSource.update${COLLECTION_CAP}Record(id, record)"
    echo ""
    echo "    override suspend fun delete${COLLECTION_CAP}Record(id: String) ="
    echo "        dataSource.delete${COLLECTION_CAP}Record(id)"
    echo ""
  fi
  echo "}"
} > "$REPO_IMPL"

echo "Scaffolded files:"
echo "  $COMMON_SRC/${COLLECTION_CAP}Record.kt"
echo "  $DS_INTERFACE"
echo "  $DS_IMPL"
echo "  $REPO_IFACE"
echo "  $REPO_IMPL"
echo ""
echo "Next steps:"
echo "  1. Fill in ${COLLECTION_CAP}Record fields to match the Firestore document schema"
echo "  2. Add @Binds entries for ${COLLECTION_CAP}Repository and ${COLLECTION_CAP}DataSource in DataModule"
echo "  3. Update docs/TECH_SPEC.md Firestore section with new collection schema"

exit 0
