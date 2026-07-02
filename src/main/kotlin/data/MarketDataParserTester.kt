package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

fun tester() {
    test1()
    test2()
}

internal fun test1() {
    val file = File("src\\main\\resources\\backtest\\yahoo\\us\\" + "meta" + ".json")
    val parsedIntoObj = YahooMarketDataParser.parse(file)
    val asStr = GsonBuilder().setPrettyPrinting().create().toJson(parsedIntoObj)
    diffFinder(file.readText(), asStr)
}

internal fun test2() {
    val file = File("src\\main\\resources\\backtest\\yahoo\\us\\" + "meta" + ".json")
    val original = YahooMarketDataParser.parse(file)

    val json = GsonBuilder()
        .setPrettyPrinting()
        .create()
        .toJson(original)

    val reparsed = Gson().fromJson(json, original::class.java)

    check(original == reparsed)
}

internal fun diffFinder(str1: String, str2: String, maxDiffs: Long = 10) {
    val lines1 = str1.lines()
    val lines2 = str2.lines()

    val maxRows = maxOf(lines1.size, lines2.size)
    var diffCount = 0 // Keep track of found differences

    for (row in 0..<maxRows) {
        val line1 = if (row < lines1.size) lines1[row] else ""
        val line2 = if (row < lines2.size) lines2[row] else ""

        val maxCols = maxOf(line1.length, line2.length)

        for (col in 0..<maxCols) {
            val c1 = if (col < line1.length) line1[col] else '∅'
            val c2 = if (col < line2.length) line2[col] else '∅'

            if (c1 != c2) {
                diffCount++
                println("[$diffCount/$maxDiffs] Difference at row ${row + 1}, column ${col + 1}: '$c1' vs '$c2'")
                println("  Line1: $line1")
                println("  Line2: $line2")
                println("-".repeat(40))

                // Stop processing completely once we hit the limit
                if (diffCount >= maxDiffs) {
                    println("Reached maximum limit of $maxDiffs differences. Stopping search.")
                    return
                }
            }
        }
    }

    if (diffCount == 0) {
        println("No differences found! The strings match perfectly.")
    }
}