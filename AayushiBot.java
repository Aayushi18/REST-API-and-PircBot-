/*
 *	Extend the main Pircbot class
 *	This file contains all the methods and implementations for the bot
 *
 *	Written by Aayushi Choudhary (AXC190000) for CS 2336 Project 1 (Fall 2020)
 */

import org.jibble.pircbot.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class AayushiBot extends PircBot 
{
	// constructor to set the name of the bot
	public AayushiBot()
	{
		this.setName("AayushiBot");
	}
	
	// method to send messages to the user when they join the channel
	public void onJoin(String channel, String sender, String login, String hostname) 
	{
		sendMessage(channel, sender + ": Welcome to Aayushi's Bot! Hope you're staying safe. :D");
		sendMessage(channel, sender + ": If you want to know what time it is, just include the keyword \"time\" in your message!");
		sendMessage(channel, sender + ": If you want to know what the weather in America is, let me know by saying \"weather (city name/zip code)\" ");
		sendMessage(channel, sender + ": If you want to look at NASA's Astronomy Picture of the Day, let me know by including the words \"NASA\" or \"space\" in your message!");					
		sendMessage(channel, sender + ": If you want the latest COVID updates, let me know by including the words \"covid\" or \"corona\" in your message.");
		sendMessage(channel, sender + ": Or you can simply ask me to tell you a joke! ");
		sendMessage(channel, sender + ": If you want to leave, say bye! :)");

	}
	
	// method to control the actions to be performed as per the message sent by the user
	public void onMessage(String channel, String sender, String login, String hostname, String message) 
	{		
		// converting the user entry to all lower case to make comparison easier
		message = message.toLowerCase();
		
		if (message.contains("time"))
		{
			getTime(channel, sender);															// if the message contains "time", the current time is displayed
		}
		
		else if (message.contains("weather"))
		{
			try {
				getWeather(message, channel, sender);											// if the message contains "weather", the weather at the user specified location is displayed
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (message.contains("nasa") || message.contains("space"))							// if the message contains "nasa" or "space", NASA's image of the day is displayed
		{
			String outputMessage = NASA_API(message, channel, sender);
			sendMessage(channel, sender + ": " + outputMessage);
		}
		
		else if (message.contains("joke"))														// if the message contains "joke", a random joke is displayed
		{
			String outputMessage = jokesAPI( message, channel, sender);
			sendMessage(channel, sender + ": " + outputMessage);
		}
		
		else if (message.contains("covid") || message.contains("corona"))						// if the message contains "covid" or "corona", the current global stats are displayed
		{
			String outputMessage = COVID_API( message, channel, sender);
			sendMessage(channel, sender + ": " + outputMessage);
		}
		
		else if (message.contains("bye") || message.contains("disconnect"))						// if the message contains "bye" or "disconnect", the bot disconnects
		{
			disconnect();			
		}
		
		else																					// if the user enters a message that can't be recognized, an error message is displayed		
		{
			sendMessage(channel, sender + ": Oops! I don't recognise that. Please send another message :)");
		}
	}
	
	// method to get time and display that to the user
	void getTime(String channel, String sender)							
	{
		String time = new java.util.Date().toString();
		sendMessage(channel, sender + ": The time right now is " + time);
	}
	
	// method to get the weather and display that to the user
	void getWeather(String message, String channel, String sender) throws IOException
	{
		String location = null, outputMessage;
		final Pattern regex = Pattern.compile("(\\d{5})");										// to extract the zipcode from message
		String[] array = message.split(" ");													// splitting the message into two - weather and location
		
		if (array.length == 2)																	// if the length is 2 words long
		{
			if (array[0].equals("weather"))																
			{
				location = array[1];															// if the first word is weather, the second word is the location										
			}			
			else
			{
				location = array[0];															// else the first word is the location
			}			
		}
		
		else if (array.length == 3)																// for cities with two word names
		{
			if (array[0].equals("weather"))
			{
				location = array[1] + "%20" + array[2];											// adding in %20 for the url			
			}			
			else
			{
				location = array[0] + "%20" + array[1];
			}			
		}
		
		else
		{
			Matcher matcher = regex.matcher(message);
			if (matcher.find())
			{
				location = matcher.group(1);													// using regex to find a match for a 5-digit number in the message entered by the user
			}
			
			else
			{
				sendMessage(channel, sender + ": Unable to find this location. ");				// display an error message if the location cannot be found
			}
		}
		
		outputMessage = weatherAPI(location);
		sendMessage(channel, sender + ":  " + outputMessage);
	}
	
	// method to implement the Weather API
	// Uses the Open Weather Map API: http://openweathermap.org/
	String weatherAPI(String location) 
	{
		// creating the API endpoint (the URL) that will be called by incorporating the city name/zipcode	
		
		String APPID = "502d4033ab0bcf76e798cdbe180ad2b7";										// using the ID provided by OpenWeather API
		String weatherURL = "http://api.openweathermap.org/data/2.5/weather?q=" + location +",US&APPID=" + APPID;
		
		try 																					// try catch block to catch and display any errors
		{
			URL url = new URL(weatherURL);														// creating the URL object
			HttpURLConnection con = (HttpURLConnection) url.openConnection();					// creating an HTTPURLConnection object
			con.setRequestMethod("GET");														// using the object to create a GET request
			
			// creating a BufferReader to read the connection inputStream
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();											// converting BufferReader to String and storing in a result variable
			while ((inputLine = in.readLine()) != null) 
			{
			    content.append(inputLine);
			}
			in.close();
			String resultMessage = parseTemp(content.toString(), location);						// calling the parseTemp method
			
			return resultMessage;
		}
		catch (Exception e)
		{return "Sorry, I am unable to process this information. Error:  " + e;}		
	}
	
	// method to parse temperature JSON
	public static String parseTemp(String json, String location)
    {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();						// new JSON object to parse JSON
        String cityName = object.get("name").getAsString();										// name of the city taken as a string
        JsonObject main = object.getAsJsonObject("main");
        
        double currentTemp = main.get("temp").getAsDouble();									// for the current temperature
        currentTemp = (currentTemp - 273.15) * 1.8 + 32;										// converts Kelvin to Fahrenheit
        
        double minTemp = main.get("temp_min").getAsDouble();									// for the minimum temperature
        minTemp = (minTemp - 273.15) * 1.8 + 32;												// converts Kelvin to Fahrenheit
        
        double maxTemp = main.get("temp_max").getAsDouble();									// for the minimum temperature
        maxTemp = (maxTemp - 273.15) * 1.8 + 32;												// converts Kelvin to Fahrenheit
        
        DecimalFormat df = new DecimalFormat("####0.0");										// for formatting the result
       
        return "The current temperature in " + cityName + " is " + df.format(currentTemp) + "F with a high of " + df.format(maxTemp) +
                "F and a low of " + df.format(minTemp) + "F." ;
    }	

	// method to implement the NASA API
	// used the NASA API: https://api.nasa.gov/
	String NASA_API(String message, String channel, String sender)
	{
		String API_KEY = "4TaPQ5XePerIDe9mHPKjVULia8dBLwiPWud2a1Jh";
		String NASA_URL = "https://api.nasa.gov/planetary/apod?api_key=" + API_KEY;
		
		try 
		{
			URL url = new URL(NASA_URL);														// creating the URL object
			HttpURLConnection con = (HttpURLConnection) url.openConnection();					// creating an HTTPURLConnection object
			con.setRequestMethod("GET");														// using the object to create a GET request
			
			// creating a BufferReader to read the connection inputStream
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();											// converting BufferReader to String and storing in a result variable
			while ((inputLine = in.readLine()) != null) 
			{
			    content.append(inputLine);
			}
			in.close();
			String resultMessage = parseNASA(content.toString(), channel, sender);				// calling the parseNASA method	
			return resultMessage;
		}
		catch (Exception e)
		{return "Sorry, I am unable to process this information. Error:  " + e;}	
	}
	
	// method to parse NASA JSON
	public String parseNASA(String json, String channel, String sender)
    {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();						// new JSON object to parse JSON
        String URL = object.get("url").getAsString();											// URL taken as a string
        String explanation = object.get("explanation").getAsString();
        String title = object.get("title").getAsString();
        
		sendMessage(channel, sender + ": Check out NASA's picture of the day - " + URL);		// displaying the required data
		sendMessage(channel, sender + ": This picture is called \"" + title + "\". Read the story behind it:");
		        
        return explanation;
    }

	// method to implement the Jokes API
	// uses the official-joke-api: https://official-joke-api.appspot.com/random_joke
	String jokesAPI(String message, String channel, String sender)
	{
		String jokesURL = "https://official-joke-api.appspot.com/random_joke";
		
		try 
		{
			URL url = new URL(jokesURL);														// creating the URL object
			HttpURLConnection con = (HttpURLConnection) url.openConnection();					// creating an HTTPURLConnection object
			con.setRequestMethod("GET");														// using the object to create a GET request
			
			// creating a BufferReader to read the connection inputStream
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();											// converting BufferReader to String and storing in a result variable
			while ((inputLine = in.readLine()) != null) 
			{
			    content.append(inputLine);
			}
			in.close();
			
			String returnMessage = parseJokes(content.toString(), channel, sender);				// calling the parseJokes method
			return returnMessage;
		}
		catch (Exception e)
		{return "Sorry, I am unable to process this information. Error:  " + e;}	
	}
	
	// method to parse the Jokes API
	public String parseJokes(String json, String channel, String sender)
    {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();						// new JSON object to parse JSON
        String setup = object.get("setup").getAsString();										// setup taken as a string
        String punchline = object.get("punchline").getAsString();								// punchline taken as string
        
		sendMessage(channel, sender + ": " + setup);
		
		return punchline;
    }
	
	// method to implement the COVID API
	// uses the COVID 19 API: https://covid19api.com/
	String COVID_API(String message, String channel, String sender)
	{
		String COVID_URL = "https://api.covid19api.com/summary";
		
		try 
		{
			URL url = new URL(COVID_URL);														// creating the URL object
			HttpURLConnection con = (HttpURLConnection) url.openConnection();					// creating an HTTPURLConnection object
			con.setRequestMethod("GET");														// using the object to create a GET request
			
			// creating a BufferReader to read the connection inputStream
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
				StringBuffer content = new StringBuffer();										// converting BufferReader to String and storing in a result variable
				while ((inputLine = in.readLine()) != null) 
			{
			    content.append(inputLine);
			}
			in.close();
			String resultMessage = parseCOVID(content.toString(), channel, sender);				// calling the parseCOVID method	
			return resultMessage;
		}
		catch (Exception e)
		{return "Sorry, I am unable to process this information. Error:  " + e;}	
	}
	
	// method to parse the COVID API
	public String parseCOVID(String json, String channel, String sender)
    {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();						// new JSON object to parse JSON
        JsonObject global = object.getAsJsonObject("Global");
        
        long newConfirmed = global.get("NewConfirmed").getAsLong();								// storing the parsed information in long variables					
        long totalConfirmed = global.get("TotalConfirmed").getAsLong();
        long totalDeaths = global.get("TotalDeaths").getAsLong();
        long newRecovered = global.get("NewRecovered").getAsLong();
        long totalRecovered = global.get("TotalRecovered").getAsLong();
        
        sendMessage(channel, sender + ": The number of new confirmed cases is: " + newConfirmed);	// displaying the required data
        sendMessage(channel, sender + ": The total number of all confirmed cases is: " + totalConfirmed);
        sendMessage(channel, sender + ": The total number of deaths is: " + totalDeaths);
        sendMessage(channel, sender + ": The total number of new recoveries is: " + newRecovered);
        
        return "The total number of recoveries is: " + totalRecovered;
    }		
}
