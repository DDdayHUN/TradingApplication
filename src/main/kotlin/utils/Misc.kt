package utils

/**
 * Formats this Double into a String with a fixed number of decimal places.
 *
 * @param value number of digits after the decimal point.
 * @return string representation of the double rounded to the specified precision.
 */
fun Double.format(value: Int): String {
    return "%.${value}f".format(this)
}