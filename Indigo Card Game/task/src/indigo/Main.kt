package indigo

import kotlin.system.exitProcess

val ranks = "2 3 4 5 6 7 8 9 10 J Q K A".split(" ").toSet()
val suits = "♦ ♥ ♠ ♣".split(" ").toSet()

class Card(private val id: Int) {
    val rank = id % 13
    val suit = id / 13
    fun isCandidateTo(other: Card) = suit == other.suit || rank == other.rank
    override fun toString() = ranks.elementAt(rank) + suits.elementAt(suit)
}

class Player(val isNPC: Boolean, var cards: Int = 0, var score: Int = 0,
             val hand: MutableList<Card> = mutableListOf<Card>()) {

    fun playCardFor(tableTopCard : Card?): Card {
        val card = if (isNPC) selectByComputer(tableTopCard) else selectByPlayer()
        hand.remove(card).also { if (isNPC) println ("Computer plays $card") }
        return card
    }

    private fun selectByComputer (tableTopCard: Card?): Card {
        println (hand.joinToString (" "))
        if (hand.size == 1) return hand.last()
        if (tableTopCard == null || hand.filter { it.isCandidateTo(tableTopCard) }.isEmpty()) {
            val cardsPerSuit = hand.groupBy { it.suit }.filter { it.value.size > 1 }.keys.sorted()
            if (cardsPerSuit.isNotEmpty()) return hand.filter { it.suit == cardsPerSuit.first() }.random()
            val cardsPerRanks = hand.groupBy { it.rank }.filter { it.value.size > 1 }.keys.sorted()
            if (cardsPerRanks.isNotEmpty()) return hand.filter { it.rank == cardsPerRanks.first() }.random()
            return hand.random()
        } else {
            val candidates = hand.filter { it.isCandidateTo(tableTopCard) }
            if (candidates.size == 1) return candidates.first()
            val someSuitCards = hand.filter { it.suit == tableTopCard.suit }
            if (someSuitCards.size > 1) return someSuitCards.random()
            val somRankCards = hand.filter { it.rank == tableTopCard.rank }
            if (somRankCards.size > 1) return somRankCards.random()
            return candidates.random()
        }
    }

    private fun selectByPlayer(): Card {
        var i = 1
        println ("Cards in hand: ${hand.joinToString(" "){"${i++})${it}"}}")
        while (true) {
            println("Choose a card to play (1-${hand.size}):")
            val answer = readLine()!!
            if (answer == "exit") println("Game Over").also { exitProcess(0) }
            if (answer.all { it.isDigit() } && answer.toInt() in 1..hand.size)
                return hand.elementAt(answer.toInt() - 1)
        }
    }
}

fun main() {
    println ("Indigo Card Game")
    val player = Player(false)
    val computer = Player(true)
    var lastWonPlayer : Player? = null
    val firstPlayer: Player
    while (true) {
        println ("Play first?")
        when (readLine()) {
            "yes" -> { firstPlayer = player; break }
            "no" -> { firstPlayer = computer; break }
        }
    }
    var currentPlayer = firstPlayer

    val deck = List(52) { Card(it) }.shuffled().toMutableList()
    val table = MutableList(4) { deck.removeLast() }
    println("Initial cards on the table: ${table.joinToString(" ")}")

    while (true) {
        if (currentPlayer.hand.isEmpty())
            if (deck.isNotEmpty()) for (i in 1..6) {   // deal 6 cards each
                player.hand.add(deck.removeLast())
                computer.hand.add(deck.removeLast())
            } else break                                       // Game over

        println (if (table.isEmpty())  "\nNo cards on the table"
        else "\n${table.size} cards on the table, and the top card is ${table.last()}")

        table.add(currentPlayer.playCardFor( if(table.isNotEmpty()) table.last() else null )) // play card

        if (table.size > 1 && table.last().isCandidateTo(table.elementAt(table.size - 2))) {  // win
            currentPlayer.cards += table.size
            currentPlayer.score += table.count { it.rank > 7 }
            table.clear()
            println ("${if(currentPlayer.isNPC) "Computer" else "Player"} wins cards")
            println ("Score: Player ${player.score} - Computer ${computer.score}")
            println ("Cards: Player ${player.cards} - Computer ${computer.cards}")
            lastWonPlayer = currentPlayer
        }
        currentPlayer = if (currentPlayer.isNPC) player else computer // next player
    }

    if (table.isNotEmpty()) {                        // process remaining cards
        println ("\n${table.size} cards on the table, and the top card is ${table.last()}")
        val remainingPlayer = lastWonPlayer ?: firstPlayer
        remainingPlayer.cards += table.size
        remainingPlayer.score += table.count { it.rank > 7 }
    } else println ("No cards on the table")

    when (player.cards.compareTo(computer.cards)) {  // process three points.
        1 -> player.score += 3
        -1 -> computer.score += 3
        else -> firstPlayer.score += 3
    }
    println ("Score: Player ${player.score} - Computer ${computer.score}")
    println ("Cards: Player ${player.cards} - Computer ${computer.cards}")
    println ("Game Over")
}