package com.narik.mania.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class JitterMode { GRB, GJK }

data class GeoPoint(val lat: Double, val lng: Double)

object JitterEngine {

    private const val METERS_PER_DEG = 111_320.0

    /** Geser titik [base] sejauh [distMeters] ke arah [bearingDeg]. */
    fun offset(base: GeoPoint, bearingDeg: Double, distMeters: Double): GeoPoint {
        val rad = bearingDeg * PI / 180.0
        val dLat = distMeters * cos(rad) / METERS_PER_DEG
        val dLng = distMeters * sin(rad) / (METERS_PER_DEG * cos(base.lat * PI / 180.0))
        return GeoPoint(base.lat + dLat, base.lng + dLng)
    }

    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val dLat = (b.lat - a.lat) * METERS_PER_DEG
        val dLng = (b.lng - a.lng) * METERS_PER_DEG * cos(a.lat * PI / 180.0)
        return sqrt(dLat * dLat + dLng * dLng)
    }

    private fun randomWithin(center: GeoPoint, radiusMeters: Double): GeoPoint {
        val dist = radiusMeters * sqrt(Random.nextDouble())
        val bearing = Random.nextDouble(0.0, 360.0)
        return offset(center, bearing, dist)
    }

    /** GRB: random-walk halus, maju sejauh [stepMeters] tiap tick, tetap dalam radius. */
    fun nextGrb(prev: GeoPoint?, center: GeoPoint, stepMeters: Double, radiusMeters: Double): GeoPoint {
        val start = prev ?: return randomWithin(center, radiusMeters)
        val bearing = Random.nextDouble(0.0, 360.0)
        val candidate = offset(start, bearing, stepMeters)
        return if (distanceMeters(center, candidate) <= radiusMeters) {
            candidate
        } else {
            randomWithin(center, radiusMeters)
        }
    }

    /** GJK: lompatan acak dalam radius, jarak minimal dari titik sebelumnya >= stepMeters. */
    fun nextGjk(prev: GeoPoint?, center: GeoPoint, stepMeters: Double, radiusMeters: Double): GeoPoint {
        repeat(10) {
            val candidate = randomWithin(center, radiusMeters)
            if (prev == null || distanceMeters(prev, candidate) >= stepMeters) return candidate
        }
        return randomWithin(center, radiusMeters)
    }
}
