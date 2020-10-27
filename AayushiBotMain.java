/*
 * This file contains the main method for PircBot implementation
 * 
 * Written by Aayushi Choudhary (AXC190000) for CS 2336 Project 1 (Fall 2020)
 */
import org.jibble.pircbot.*;

public class AayushiBotMain 
{
	public static void main(String[] args) throws Exception {
		
		// starting the bot up
		AayushiBot bot = new AayushiBot();
		
		// enable debugging output		
		bot.setVerbose(true);
		
		// connect to the IRC server
		bot.connect("irc.freenode.net");

		// join the #AChannel channel
		bot.joinChannel("#AChannel");
	}
}
