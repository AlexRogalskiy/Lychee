@file:Suppress("NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")
package net.aquadc.persistence.struct

import androidx.annotation.RestrictTo


/**
 * Returns an empty set of fields.
 * Useful when you need a `var` of type `FieldSet<SCH, FieldDef<SCH, *, *>>` for filling it with `+=` later.
 */
inline fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> emptyFieldSet(dummy: Nothing? = null): FieldSet<SCH, F> =
        FieldSet(0L)

/**
 * Returns an empty set of fields.
 * Useful for passing anywhere because it is a subtype of
 * both `FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>` abd `FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>`.
 * [SCH] of output type is still not [Nothing] because it is safer to leave it invariant everywhere.
 */
inline fun <SCH : Schema<SCH>> emptyFieldSet(): FieldSet<SCH, Nothing> =
        FieldSet(0L)

/**
 * Returns a set of a single field.
 */
inline fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> F.asFieldSet(): FieldSet<SCH, F> =
        FieldSet(1L shl ordinal.toInt())

/**
 * Filters fields of [this] [Schema] with a [predicate] returning a set of suitable ones.
 */
inline fun <SCH : Schema<SCH>> SCH.fieldsWhere(predicate: (FieldDef<SCH, *, *>) -> Boolean): FieldSet<SCH, FieldDef<SCH, *, *>> {
    var mask = 0L

    var bit = 1L
    val fields = fields
    for (index in fields.indices) {
        if (predicate(fields[index])) {
            mask = mask or bit
        }
        bit = bit shl 1
    }

    return FieldSet(mask)
}


/**
 * Returns a set with all fields of [this] [Schema].
 */
fun <SCH : Schema<SCH>> SCH.allFieldSet(): FieldSet<SCH, FieldDef<SCH, *, *>> =
        FieldSet(
                fields.size.let { size ->
                    // (1L shl size) - 1   :  1L shl 64  will overflow to 1
                    // -1L ushr (64 - size): -1L ushr 64 will remain -1L
                    // the last one is okay, assuming that zero-field structs are prohibited
                    -1L ushr (64 - size)
                }
        )

/**
 * Returns a set of all [FieldDef.Mutable] fields of [this] [Schema].
 */
fun <SCH : Schema<SCH>> SCH.mutableFieldSet(): FieldSet<SCH, FieldDef.Mutable<SCH, *, *>> =
        fieldsWhere { it is FieldDef.Mutable } as FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>

/**
 * Returns a set of all [FieldDef.Immutable] fields of [this] [Schema].
 */
fun <SCH : Schema<SCH>> SCH.immutableFieldSet(): FieldSet<SCH, FieldDef.Immutable<SCH, *, *>> =
        fieldsWhere { it is FieldDef.Immutable } as FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>


/**
 * Returns the a set representing a union of [this] and [other] [FieldDef]s.
 */
inline operator fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> F.plus(other: F): FieldSet<SCH, F> =
        FieldSet((1L shl ordinal.toInt()) or (1L shl other.ordinal.toInt()))

/**
 * Returns a set representing a union of [this] set and [other] field.
 */
inline operator fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>, G : F, H : F> FieldSet<SCH, G>.plus(other: H): FieldSet<SCH, F> =
        FieldSet(bitSet or (1L shl other.ordinal.toInt()))

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectAnyAny") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef<SCH, *, *>>): FieldSet<SCH, FieldDef<SCH, *, *>> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectAnyMut") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>): FieldSet<SCH, FieldDef.Mutable<SCH, *, *>> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectAnyImm") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef<SCH, *, *>>.intersect(
        other: FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>
): FieldSet<SCH, FieldDef.Immutable<SCH, *, *>> = FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectAnyNuff") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef<SCH, *, *>>
        .intersect(other: FieldSet<SCH, Nothing>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectMutAny") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef<SCH, *, *>>): FieldSet<SCH, FieldDef.Mutable<SCH, *, *>> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectMutMut") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>): FieldSet<SCH, FieldDef.Mutable<SCH, *, *>> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectMutImm") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectMutNuff") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, Nothing>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectImmAny") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef<SCH, *, *>>): FieldSet<SCH, FieldDef.Immutable<SCH, *, *>> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectImmMut") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectImmImm") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>): FieldSet<SCH, FieldDef.Immutable<SCH, *, *>> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectImmNuff") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>
        .intersect(other: FieldSet<SCH, Nothing>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectNuffAny") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, Nothing>
        .intersect(other: FieldSet<SCH, FieldDef<SCH, *, *>>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectNuffMut") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, Nothing>
        .intersect(other: FieldSet<SCH, FieldDef.Mutable<SCH, *, *>>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectNuffImm") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, Nothing>
        .intersect(other: FieldSet<SCH, FieldDef.Immutable<SCH, *, *>>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/** Returns a set representing intersection of [this] set and the [other] set. */
@JvmName("intersectNuffNuff") inline infix fun <SCH : Schema<SCH>> FieldSet<SCH, Nothing>
        .intersect(other: FieldSet<SCH, Nothing>): FieldSet<SCH, Nothing> =
        FieldSet(this.bitSet and other.bitSet)

/**
 * Returns a set equal to [this] without [other] field.
 */
inline operator fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>, G : F, H : F> FieldSet<SCH, G>.minus(other: H): FieldSet<SCH, F> =
        FieldSet(bitSet and (1L shl other.ordinal.toInt()).inv())

/**
 * Returns a set equal to [this] without fields from [other] set.
 */
inline operator fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> FieldSet<SCH, F>.minus(other: FieldSet<SCH, F>): FieldSet<SCH, F> =
        FieldSet(bitSet and other.bitSet.inv())
        // this:          0000000000000000011111111111111111
        // other:         0000000000000010101010101010101010
        // ~other:        1111111111111101010101010101010101
        // this & ~other: 0000000000000000010101010101010101

/**
 * Checks whether [this] set contains that [field].
 */
inline operator fun <SCH : Schema<SCH>> FieldSet<SCH, *>.contains(field: FieldDef<SCH, *, *>): Boolean =
        (bitSet and (1L shl field.ordinal.toInt())) != 0L

/**
 * Number of fields in this set.
 */
val FieldSet<*, *>.size: Byte
    get() = java.lang.Long.bitCount(bitSet).toByte()

/**
 * Whether this set is empty.
 */
val FieldSet<*, *>.isEmpty: Boolean
    get() = bitSet == 0L

/**
 * Returns index of [field] in this set.
 * Memory layouts of partial structs are built on top of this.
 */
fun <SCH : Schema<SCH>> FieldSet<SCH, *>.indexOf(field: FieldDef<SCH, *, *>): Byte {
    val ord = field.ordinal
    val one = 1L shl ord.toInt()
    return if ((bitSet and one) == 0L) -1 else java.lang.Long.bitCount(bitSet and lowerOnes(ord)).toByte()
}

fun <SCH : Schema<SCH>> SCH.toString(fields: FieldSet<SCH, *>): String =
        if (fields.isEmpty) "[]"
        else buildString {
            append('[')
            forEach<SCH, FieldDef<SCH, *, *>>(fields) { field ->
                append(nameOf(field)).append(", ")
            }
            setLength(length - 2)
            append(']')
        }

/**
 * Invokes [func] on each element of the [set].
 */
inline fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> SCH.forEach(set: FieldSet<SCH, F>, func: SCH.(F) -> Unit) {
    var ord = 0
    var mask = set.bitSet
    while (mask != 0L) {
        if ((mask and 1L) == 1L) {
            func(this, fields[ord] as F)
        }

        mask = mask ushr 1
        ord++
    }
}

/**
 * Asserts [FieldSet.size] is 1 and returns this [FieldDef].
 */
fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> SCH.single(set: FieldSet<SCH, F>): F {
    var ord = 0
    var mask = set.bitSet
    while ((mask and 1L) == 0L) {
        mask = mask ushr 1
        ord++
    }
    check(mask == 1L)
    return fields[ord] as F
}

/**
 * Invokes [func] on each element of the [set].
 */
inline fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>> SCH.forEachIndexed(set: FieldSet<SCH, F>, func: (Int, F) -> Unit) {
    val fields = fields
    var idx = 0
    var ord = 0
    var mask = set.bitSet
    while (mask != 0L) {
        if ((mask and 1L) == 1L) {
            func(idx++, fields[ord] as F)
        }

        mask = mask ushr 1
        ord++
    }
}

/**
 * Invokes [func] on each element of the [set].
 */
inline fun <SCH : Schema<SCH>, F : FieldDef<SCH, *, *>, reified R> SCH.mapIndexed(set: FieldSet<SCH, F>, func: (Int, F) -> R): Array<R> {
    val fields = fields
    val out = arrayOfNulls<R>(set.size.toInt())
    var idx = 0
    var ord = 0
    var mask = set.bitSet
    while (mask != 0L) {
        if ((mask and 1L) == 1L) {
            out[idx] = func(idx, fields[ord] as F)
            idx += 1
        }

        mask = mask ushr 1
        ord++
    }
    return out as Array<R>
}

typealias FldSet<SCH> = FieldSet<SCH, FieldDef<SCH, *, *>>

/**
 * Represents an allocation-less [Set]<FieldDef<SCH, FLD>>.
 */
inline class FieldSet<SCH : Schema<SCH>, out FLD : FieldDef<SCH, *, *>>
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) constructor(
        /**
         * A value with bits set according to fields present.
         * A set considered to be 'containing field f' when bitmask has a bit `1 << f.ordinal` set.
         * This value is a part of serialization ABI and has stable format
         * as it is written to and read from persistent storages.
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) val bitSet: Long
)

private fun lowerOnes(r: Byte): Long =
        ((1L shl r.toInt()) - 1L)
