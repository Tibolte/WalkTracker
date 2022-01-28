package fr.northborders.walktracker

import fr.northborders.walktracker.data.db.PhotoEntity
import java.util.concurrent.ThreadLocalRandom

class PhotoFactory {
    companion object Factory {

        fun makePhotoEntity(): PhotoEntity {
            return PhotoEntity(
                randomInt(),
                randomString(),
                randomString(),
                randomString(),
                randomString()
            )
        }

        fun randomString(): String {
            return java.util.UUID.randomUUID().toString()
        }

        fun randomInt(): Int {
            return ThreadLocalRandom.current().nextInt(0, 1000 + 1)
        }

        fun randomLong(): Long {
            return randomInt().toLong()
        }

        fun randomBoolean(): Boolean {
            return Math.random() < 0.5
        }

        fun makeStringList(count: Int): List<String> {
            val items: MutableList<String> = mutableListOf()
            repeat(count) {
                items.add(randomString())
            }
            return items
        }
    }
}