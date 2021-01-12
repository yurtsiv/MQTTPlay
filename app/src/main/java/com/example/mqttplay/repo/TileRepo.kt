package com.example.mqttplay.repo

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

enum class TileType {
    RECURRING,
    BUTTON,
    UNKNOWN
}

data class RecurringTileTime(
    val hour: Int,
    val minute: Int
)

data class Tile(
    val id: String? = null,
    val brokerId: String,
    val topic: String,
    val value: String?,
    val qos: Int,
    val retainMessage: Boolean?,
    val type: TileType,
    val recurringTime: RecurringTileTime?
)

class TileRepo {
    companion object {
        private val db = Firebase.firestore
        const val COLLECTION = "tiles"

        private fun stringToTileType(str: String?): TileType {
           return when(str) {
               "RECURRING" -> TileType.RECURRING
               "BUTTON" -> TileType.BUTTON
               else -> TileType.UNKNOWN
           }
        }

        private fun docToTile(document: DocumentSnapshot): Tile {
            var recurringTime: RecurringTileTime? = null

            if (document.data?.get("recurringTime") != null) {
                val hashMap = document.data?.get("recurringTime") as HashMap<String, Long>
                recurringTime = RecurringTileTime(
                    (hashMap["hour"] as Long).toInt(),
                    (hashMap["minute"] as Long).toInt()
                )
            }

            return Tile(
                document.id,
                document.data?.get("brokerId") as String,
                document.data?.get("topic") as String,
                document.data?.get("value") as String?,
                (document.data?.get("qos") as Long).toInt(),
                document.data?.get("retainMessage") as Boolean,
                stringToTileType(document.data?.get("type") as String),
                recurringTime
            )
        }

        private fun tileToHashMap(tile: Tile): HashMap<String, Any?> {
            var recurringTime: HashMap<String, Int>? = null
            if (tile.recurringTime != null) {
                recurringTime = hashMapOf(
                    "hour" to tile.recurringTime?.hour,
                    "minute" to tile.recurringTime?.minute
                )
            }

            return hashMapOf(
                "brokerId" to tile.brokerId,
                "topic" to tile.topic,
                "value" to tile.value,
                "qos" to tile.qos,
                "retainMessage" to tile.retainMessage,
                "type" to tile.type,
                "recurringTime" to recurringTime
            )
        }

        suspend fun listAllForBroker(brokerId: String): List<Tile> {
            return suspendCoroutine { cont ->
                db.collection(COLLECTION)
                    .get()
                    .addOnSuccessListener { documents ->
                        val res = documents.map { docToTile(it) }
                        cont.resume(res)
                    }
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            }
        }

        suspend fun fetchSingle(id: String): Tile {
            return suspendCoroutine { cont ->
                db.document("${COLLECTION}/${id}")
                    .get()
                    .addOnSuccessListener { doc ->
                        cont.resume(docToTile(doc))
                    }
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            }
        }

        private suspend fun create(tile: Tile): String {
            return suspendCoroutine { cont ->
                db.collection(COLLECTION)
                    .add(tileToHashMap(tile))
                    .addOnSuccessListener { documentReference ->
                        cont.resume(documentReference.id)
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            }
        }

        private suspend fun edit(id: String, tile: Tile): String {
            return suspendCoroutine { cont ->
                db.document("${COLLECTION}/${id}")
                    .set(tileToHashMap(tile))
                    .addOnSuccessListener {
                        cont.resume(id ?: "")
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            }
        }

        suspend fun save(tile: Tile): String {
            return if (tile.id == null) {
                create(tile);
            } else {
                edit(tile.id, tile);
            }
        }

        suspend fun remove(id: String) {
            // TODO: remove tiles also
            return suspendCoroutine { cont ->
                db.document("${COLLECTION}/${id}")
                    .delete()
                    .addOnSuccessListener {
                        cont.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            }
        }
    }
}