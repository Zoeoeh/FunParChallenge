package c25

import org.jcsp.awt.*
import org.jcsp.groovy.*
import org.jcsp.lang.*
import java.awt.*
import java.awt.Color.*
import org.jcsp.net2.*;
import org.jcsp.net2.tcpip.*;
import org.jcsp.net2.mobile.*;
import java.awt.event.*

class PlayerManager implements CSProcess {
	DisplayList dList
	ChannelOutputList playerNames
	ChannelOutputList pairsWon

	ChannelInput withdrawButton
	ChannelInput nextButton
	ChannelOutput getValidPoint
	ChannelInput validPoint
	ChannelOutput nextPairConfig


	ChannelOutput playerManLoc

	ChannelInput initialisePlayer

	ChannelOutput toInterface

	int maxPlayers = 8
	int side = 50
	int minPairs = 3
	int maxPairs = 6
	int boardSize = 6

	void run(){

		int gap = 5
		def offset = [gap, gap]
		int graphicsPos = (side / 2)
		def rectSize = ((side+gap) *boardSize) + gap

		def displaySize = 4 + (5 * boardSize * boardSize)
		GraphicsCommand[] display = new GraphicsCommand[displaySize]
		GraphicsCommand[] changeGraphics = new GraphicsCommand[5]
		changeGraphics[0] = new GraphicsCommand.SetColor(Color.WHITE)
		changeGraphics[1] = new GraphicsCommand.FillRect(0, 0, 0, 0)
		changeGraphics[2] = new GraphicsCommand.SetColor(Color.BLACK)
		changeGraphics[3] = new GraphicsCommand.DrawRect(0, 0, 0, 0)
		changeGraphics[4] = new GraphicsCommand.DrawString("   ",graphicsPos,graphicsPos)

		def createBoard = {
			println "update board"
			display[0] = new GraphicsCommand.SetColor(Color.WHITE)
			display[1] = new GraphicsCommand.FillRect(0, 0, rectSize, rectSize)
			display[2] = new GraphicsCommand.SetColor(Color.BLACK)
			display[3] = new GraphicsCommand.DrawRect(0, 0, rectSize, rectSize)
			def cg = 4
			for ( x in 0..(boardSize-1)){
				for ( y in 0..(boardSize-1)){
					def int xPos = offset[0]+(gap*x)+ (side*x)
					def int yPos = offset[1]+(gap*y)+ (side*y)
					//print " $x, $y, $xPos, $yPos, $cg, "
					display[cg] = new GraphicsCommand.SetColor(Color.WHITE)
					cg = cg+1
					display[cg] = new GraphicsCommand.FillRect(xPos, yPos, side, side)
					cg = cg+1
					display[cg] = new GraphicsCommand.SetColor(Color.BLACK)
					cg = cg+1
					display[cg] = new GraphicsCommand.DrawRect(xPos, yPos, side, side)
					cg = cg+1
					xPos = xPos + graphicsPos
					yPos = yPos + graphicsPos
					display[cg] = new GraphicsCommand.DrawString("   ",xPos, yPos)
					//println "$cg"
					cg = cg+1
				}
			}
		} // end createBoard

		def pairLocations = []
		def colours = [Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.PINK]

		def changePairs = {x, y, colour, p ->
			def int xPos = offset[0]+(gap*x)+ (side*x)
			def int yPos = offset[1]+(gap*y)+ (side*y)
			changeGraphics[0] = new GraphicsCommand.SetColor(colour)
			changeGraphics[1] = new GraphicsCommand.FillRect(xPos, yPos, side, side)
			changeGraphics[2] = new GraphicsCommand.SetColor(Color.BLACK)
			changeGraphics[3] = new GraphicsCommand.DrawRect(xPos, yPos, side, side)
			xPos = xPos + graphicsPos
			yPos = yPos + graphicsPos
			if ( p >= 0)
				changeGraphics[4] = new GraphicsCommand.DrawString("   " + p, xPos, yPos)
			else
				changeGraphics[4] = new GraphicsCommand.DrawString(" ??", xPos, yPos)
			dList.change(changeGraphics, 4 + (x*5*boardSize) + (y*5))
		}



		def outerAlt = new ALT([validPoint, withdrawButton])
		def innerAlt = new ALT([nextButton, withdrawButton])
		def NEXT = 0
		def VALIDPOINT = 0
		def WITHDRAW = 1
		createBoard()
		dList.set(display)

		// removed stuff here

		// wait for reading
		initialisePlayer.read()

		// create input channel address from controller after node
		def fromController = NetChannel.net2one()
		def playerLocation = fromController.getLocation()

		playerManLoc.write(playerLocation) // response to enrol player

		println "sent waiting"
		def e = (SendGameDetails)fromController.read() // read from controller set up
		println "Hello from controller $e"

		// create output to controller
		def toController = NetChannel.any2net(e.controllerLocation)
		toController.write("yay")

		def playerId = e.playerId

		// send info to interface
		initialisePlayer.read()
		playerManLoc.write(e)

		def enroled = true

		// main loop
		while (enroled)
		{
			println "wait for update"
			def gameDetails = (GameDetails)fromController.read()
			def gameId = gameDetails.gameId
			println "updated"

			def chosenPairs = [null, null]
			createBoard()
			dList.change (display, 0)

			// player turn
			playerManLoc.write(gameDetails.playerNames[gameDetails.currentPlayer])

			def playerMap = gameDetails.playerDetails
			def pairsMap = gameDetails.pairsSpecification
			def playerIds = playerMap.keySet()
			playerIds.each { p ->
				def pData = playerMap.get(p)
				playerNames[p].write(pData[0])
				pairsWon[p].write(" " + pData[1])
			}
			println 'kill me'
			// now use pairsMap to create the board
			def pairLocs = pairsMap.keySet()
			pairLocs.each {loc ->
				changePairs(loc[0], loc[1], Color.LIGHT_GRAY, -1)
			}


			//def currentPair = 0
			//def notMatched = true

			def selection = gameDetails.currentSelection

			// highlight any chosen squares
			for(i in 0..selection.size()-1)
			{
				def squarePoint = selection[i]

				def pairData = pairsMap.get(squarePoint)

				if (squarePoint != null && pairData != null)
				{
					changePairs(squarePoint[0], squarePoint[1], pairData[1], pairData[0])
				}
			}

			while (gameDetails.currentPlayer == playerId)
			{

				def update = fromController.read()
				println "fromcon"

				if (update instanceof GameDetails)
				{
					gameDetails = update
					gameId = gameDetails.gameId

					// player turn
					playerManLoc.write(gameDetails.playerNames[gameDetails.currentPlayer])

					println "UPDATED"

					playerMap = gameDetails.playerDetails
					pairsMap = gameDetails.pairsSpecification
					playerIds = playerMap.keySet()
					playerIds.each { p ->
						def pData = playerMap.get(p)
						playerNames[p].write(pData[0])
						pairsWon[p].write(" " + pData[1])
					}

					// now use pairsMap to create the board
					pairLocs = pairsMap.keySet()
				}
				else
				{
					println "ASK FOR POINT"
					getValidPoint.write (new GetValidPoint( side: side,
					gap: gap,
					pairsMap: pairsMap))
					switch ( outerAlt.select() )
					{
						case WITHDRAW:
							withdrawButton.read()
							toController.write(new WithdrawFromGame(id: playerId))
							enroled = false
							break
						case VALIDPOINT:
						// if not turn return

							def coord = (SquareCoords)validPoint.read()

							def vPoint = coord.location

							def pairData = pairsMap.get(vPoint)
							println "click and change colour"
							changePairs(vPoint[0], vPoint[1], pairData[1], pairData[0])

							toController.write(coord)
							println "after write square"
					}
				}
			}

		} // end enrol while
	} // end run
} // end class


