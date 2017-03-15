package c25

import org.jcsp.lang.CSProcess
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.ChannelOutput;
import org.jcsp.net2.tcpip.TCPIPNodeAddress

import org.jcsp.awt.*
import org.jcsp.groovy.*
import org.jcsp.lang.*
import java.awt.*
import java.awt.Color.*
import org.jcsp.net2.*;
import org.jcsp.net2.tcpip.*;
import org.jcsp.net2.mobile.*;
import java.awt.event.*

class EnrolPlayer implements CSProcess {
	def name = ""
	def toPlayerChannelLocation = null
	
	
	ChannelOutput IPlabel
	ChannelInput IPfield
	ChannelOutput IPconfig
	
	
	@Override
	public void run() {
		IPlabel.write("What is your name?")
		name = IPfield.read()
		IPconfig.write(" ")
		IPlabel.write("What is the IP address of the game controller?")
		def controllerIP = IPfield.read().trim()
		IPconfig.write(" ")
		IPlabel.write("Connecting to the GameController")
		
		
		println "connected"
		
		// create Node and Net Channel Addresses
		def nodeAddr = new TCPIPNodeAddress (4000)
		Node.getInstance().init (nodeAddr)
		def toControllerAddr = new TCPIPNodeAddress ( controllerIP, 3000)
		def toController = NetChannel.any2net(toControllerAddr, 50 )

		println "to controller fin"
		//def fromController = NetChannel.net2one()
		//def fromControllerLoc = fromController.getLocation()
		
		// connect to game controller
		IPconfig.write("Now Connected - sending your name to Controller")
		
		// replace with send to player
		def enrolEvent = new EnrolEvent( name: name)
			//toPlayerChannelLocation: fromControllerLoc)
		println " sending $name"
		toController.write(enrolEvent) // Request as client to controller server
		println " sent $name"
		
	//	def enrolDetails = (EnrolDetails)fromController.read() 	// read response from controller server
		
		/*
		def myPlayerId = enrolDetails.id
		def enroled = true
		def unclaimedPairs = 0
		if (myPlayerId == -1) {
			enroled = false
			IPlabel.write("Sorry " + playerName + ", there are too many players enroled in this PAIRS game")
			IPconfig.write("  Please close the game window")
		}
		else {
			IPlabel.write("Hi " + playerName + ", you are now enroled in the PAIRS game")
			IPconfig.write(" ")
			*/
	
	}
}
