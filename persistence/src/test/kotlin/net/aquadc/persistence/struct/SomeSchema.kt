package net.aquadc.persistence.struct

import net.aquadc.persistence.type.int
import net.aquadc.persistence.type.long
import net.aquadc.persistence.type.string


object SomeSchema : Schema<SomeSchema>() {
    val A = "a" let string
    val B = "b" let int
    val C = "c" mut long
}
