package c25

import java.io.Serializable;

class GameDetails implements Serializable {
	def gameId
	def playerDetails = null
	def pairsSpecification = null
	// update information
	
	def currentPlayer
	def currentSelection
	
	def playerNames
}
