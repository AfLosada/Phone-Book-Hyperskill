package phonebook

import java.io.File
import kotlin.math.floor
import kotlin.math.sqrt

fun main() {
    val path = System.getProperty("user.dir")
    val namesToFind = File("$path/find.txt").readLines()
    val directory = File("$path/directory.txt").readLines()
    val directoryOfNames = directory.map {
        val splitNames = it.split(" ")
        splitNames.filterIndexed { index, _ -> index != 0 }.joinToString(" ")
    }


    println("Start searching... (linear sort)")
    val linearStartTime = System.currentTimeMillis()
    val linearFoundNames = linearSearch(directory, namesToFind)
    val linearEndTime = System.currentTimeMillis()
    val difference = (linearEndTime - linearStartTime)
    printFoundResults(linearFoundNames, namesToFind, difference)


    var bubbleSortFailed = false
    var bubbleFoundNames: List<String>
    println()
    println("Start searching... (bubble sort + jump search)")
    var bubbleSortTime: Long = 0
    val bubbleStartTime = System.currentTimeMillis()
    try {
        bubbleFoundNames = bubbleAndJumpSearch(directoryOfNames, namesToFind, difference * 10)
    } catch (e: SortException) {
        bubbleSortFailed = true
        bubbleFoundNames = linearSearch(directory, namesToFind)
        val currentTime = System.currentTimeMillis()
        bubbleSortTime = (currentTime - bubbleStartTime)
    }
    val bubbleEndTime = System.currentTimeMillis()
    val bubbleDifference = (bubbleEndTime - bubbleStartTime)
    printFoundResults(bubbleFoundNames, namesToFind, bubbleDifference, bubbleSortFailed, "linear")
    println("Sorting time: ${bubbleSortTime.formatTime()}")
    println("Searching time: ${(bubbleDifference - bubbleSortTime).formatTime()}")

    println()
    println("Start searching... (quick sort + binary search)...")
    val quickSortStartTime = System.currentTimeMillis()
    val quickSortNamesFound = quickSortAndBinarySearch(directoryOfNames, namesToFind)
    val quickSortEndTime = System.currentTimeMillis()
    println("Found ${quickSortNamesFound.size} / ${namesToFind.size} entries. Time taken: ${(quickSortEndTime - quickSortStartTime).formatTime()}")

    println()
    println("Start searching... (hash table)...")
    val hashStartTime = System.currentTimeMillis()
    val hashCreationStartTime = System.currentTimeMillis()
    val hashSet = directoryOfNames.toHashSet()
    val hashCreationEndTime = System.currentTimeMillis()
    val hashCreationDuration = (hashCreationEndTime - hashCreationStartTime)
    val hashNamesFound = namesToFind.filter { name -> hashSet.contains(name) }
    val hashEndTime = System.currentTimeMillis()
    val hashSearchDuration = (hashEndTime - hashStartTime)
    println("Found ${hashNamesFound.size} / ${namesToFind.size} entries. Time taken: ${hashSearchDuration.formatTime()}")
    println("Creating time: ${hashCreationDuration.formatTime()}")
    println("Searching time: ${(hashSearchDuration - hashCreationDuration).formatTime()}")


}

class SortException : Exception()

fun printFoundResults(
    foundNames: List<String>,
    namesToFind: List<String>,
    difference: Long,
    stopped: Boolean = false,
    movedTo: String = "",
) {
    println(
        "Found ${foundNames.size} / ${namesToFind.size} entries. Time taken: ${
            String.format(
                "%1\$tM min. %1\$tS sec. %1\$tL ms. ", difference
            )
        }${if (stopped) "- STOPPED, moved to $movedTo search" else ""}"
    )
}

fun Long.formatTime(): String {
    return String.format(
        "%1\$tM min. %1\$tS sec. %1\$tL ms.", this
    )
}

fun linearSearch(directory: List<String>, namesToFind: List<String>): List<String> {
    return namesToFind.filter { name -> directory.find { it.contains(name) } != null }
}

fun bubbleAndJumpSearch(directory: List<String>, namesToFind: List<String>, maxTime: Long): List<String> {
    return namesToFind.filter { name -> directory.bubbleSort(true, maxTime).jumpSearch(name) != null }
}

fun quickSortAndBinarySearch(directory: List<String>, namesToFind: List<String>): List<String> {
    val quickSortStartTime = System.currentTimeMillis()
    val sortedDirectory = directory.quickSort()
    val quickSortEndTime = System.currentTimeMillis()

    val binarySearchStartTime = System.currentTimeMillis()
    println("Sorting time: ${(quickSortEndTime - quickSortStartTime).formatTime()}")
    val foundNames = namesToFind.filter { name ->
        sortedDirectory.binarySearch(name) != null
    }
    val binarySearchEndTime = System.currentTimeMillis()
    println("Searching time: ${(binarySearchEndTime - binarySearchStartTime).formatTime()}")
    return foundNames
}

fun <T : Comparable<T>> List<T>.bubbleSort(ascending: Boolean = true, maxTime: Long): List<T> {
    val startTime = System.currentTimeMillis()
    val mutableList = this.toMutableList()
    var isSorted = false
    while (!isSorted) {
        var changes = 0
        for (i in 0 until mutableList.size - 1) {
            val currentTime = System.currentTimeMillis()
            val currentDuration = currentTime - startTime
            if (currentDuration >= maxTime) throw SortException()
            val current = mutableList[i]
            val next = mutableList[i + 1]
            val needsSwitching = when (ascending) {
                false -> current < next
                true -> current > next
            }
            if (needsSwitching) {
                mutableList.switchPositions(i, i + 1)
                changes++
            }
        }
        isSorted = changes == 0
    }
    val currentTime = System.currentTimeMillis()
    println("Sorting time: ${(currentTime - startTime).formatTime()}")
    return mutableList.toList()
}

fun <T : Comparable<T>> List<T>.quickSort(): List<T> {
    if (this.isEmpty()) {
        return this
    }
    val pivot = this.last()
    val lessThanPivot = mutableListOf<T>()
    val greaterThanPivot = mutableListOf<T>()
    this.subList(0, this.size - 1).forEach { item ->
        if (item <= pivot) lessThanPivot.add(item)
        else greaterThanPivot.add(item)
    }
    return lessThanPivot.quickSort() + pivot + greaterThanPivot.quickSort()
}

fun <T> MutableList<T>.switchPositions(firstPosition: Int, secondPosition: Int) {
    val temp = this[secondPosition]
    val firstObject = this[firstPosition]
    this[secondPosition] = firstObject
    this[firstPosition] = temp
}

infix fun <T : Comparable<T>> List<T>.jumpSearch(itemToSearch: T): T? {
    val jumpSize = floor(sqrt(this.size.toDouble())).toInt()
    val splitList = this.windowed(jumpSize, jumpSize)
    val blockToSearch =
        splitList.find { block: List<T> -> block.first() <= itemToSearch && block.last() >= itemToSearch }
    return blockToSearch?.reversed()?.find { it == itemToSearch }
}

infix fun <T : Comparable<T>> List<T>.binarySearch(itemToSearch: T): T? {
    var left = 1
    var right = this.size - 1
    while (left <= right) {
        val middle = floor((left + right) / 2.0).toInt()
        val middleItem = this[middle]
        if (middleItem == itemToSearch) {
            return middleItem
        } else if (middleItem > itemToSearch) {
            right = middle - 1
        } else {
            left = middle + 1
        }
    }
    return null
}