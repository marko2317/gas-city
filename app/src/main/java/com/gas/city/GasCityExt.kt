package com.gas.city

@ExperimentalUnsignedTypes
fun ByteArray.toHex() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }