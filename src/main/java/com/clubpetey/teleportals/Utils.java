package com.clubpetey.teleportals;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static final Integer parseInt(String s, int def) {
    	if (s == null || s.isEmpty()) return def;
    	try {
    		return Integer.parseInt(s);
    	} catch (Throwable t) {} //munch
    	return def;
    }
    
    public static final Float parseFloat(String s, float def) {
    	if (s == null || s.isEmpty()) return def;
    	try {
    		return Float.parseFloat(s);
    	} catch (Throwable t) {} //munch
		return def;
    }
    
	public static String[] getMultiLine(String line, BufferedReader in, final char start, final char end) throws IOException {
		line = line.trim();
		List<String> list = new ArrayList<String>();
		boolean done = false;
		if (line.charAt(0) == start) {
			while (in.ready() && !done) {
				line = in.readLine().trim();
				done = (line.charAt(0) == end);
				list.add(line);
			}
			if (!done) Teleportals.logger.error("Bad MultiLine format: missing " + end);
		} else {
			list.add(line);
		}
		return (String[])list.toArray();
	}    
    
	// Parses a regular command line and returns it as a string array
	public static String[] getCmdArray(String cmd) { 		
	    ArrayList<String> cmdArray = new ArrayList<String>();
	    StringBuffer argBuffer = new StringBuffer();
	    char[] quotes = {'"', '\''};
	    char theChar = 0, protect = '\\', separate = ' ';
	    int cursor = 0;
	    cmd = cmd.trim();
	    while(cursor < cmd.length()) {
	        theChar = cmd.charAt(cursor);

	        // Handle protected characters
	        if(theChar == protect) {
	            if(cursor + 1 < cmd.length()) {
	                char protectedChar = cmd.charAt(cursor + 1);
	                argBuffer.append(protectedChar);
	                cursor += 2;
	            } else
	                return null; // Unprotected \ at end of cmd
	        }
	        // Handle quoted args
	        else if(inArray(theChar, quotes)) {
	            int nextQuote = cmd.indexOf(theChar, cursor + 1);
	            if(nextQuote != -1) {
	                cmdArray.add(cmd.substring(cursor + 1, nextQuote));
	                cursor = nextQuote + 1;
	            }
	            else
	                return null; // Unprotected, unclosed quote
	        }

	        // Handle separator
	        else if(theChar == separate) {
	            if(argBuffer.length() != 0)
	                cmdArray.add(argBuffer.toString());
	            argBuffer.setLength(0);
	            cursor++;
	        } else {
	            argBuffer.append(theChar);
	            cursor++;
	        }
	    }

	    if(theChar != 0) // Handle the last argument (doesn't have a space after it)
	        cmdArray.add(argBuffer.toString());

	    return cmdArray.toArray(new String[cmdArray.size()]);
	}

	private static boolean inArray(char c, char[] list) {
		for (int i=0;i<list.length-1;i++)
			if (list[i] == c) return true;
		return false;
	}	
	 
}

