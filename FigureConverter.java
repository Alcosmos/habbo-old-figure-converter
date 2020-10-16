package com.helena.http.figure;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class FigureConverter {
	/**
	 * Transforms an old figure from the old format to the new avatarimage one.
	 * Sandals are broken because of Habbo's own avatarimage; don't blame me.
	 * 
	 * @param oldFigure A figure with the old figure format, e.g. 5960162001715017400165001
	 * @return A new figure following the new figure format, e.g. hr-596-49.hd-620-1026.ch-650-1284.lg-715-1200.sh-740-1150.ha-0-49
	 */
	public static String convertOldToNew(String oldFigure) {
		int start = 0;
		String[] partsString = new String[10];
		int[] increase_start = {0, 5, 10, 15, 20};
		
		for (int i = 0; i < 10; i++) {
			int length = 2;
			for (int increase : increase_start) {
				if (increase == start) length = 3;
			}
			partsString[i] = oldFigure.substring(start, start + length);
			start = start + length;
		}
		int[] parts = new int[partsString.length];
		
		for (int i = 0; i < parts.length; i++) {
			parts[i] = Integer.parseInt(partsString[i]);
		}
		
		/*
		 * Notice that sh parts[6] contains a quick hack because the old sandals
		 * are broken is $ulake's imager and the newer 3206 ones are similar.
		 */
		
		String buildFigure;
		buildFigure = "hr-" + parts[0] + "-" + convertOldColorToNew("hr", parts[0], parts[1]);
		buildFigure += ".hd-" + parts[2] + "-" + convertOldColorToNew("hd", parts[2], parts[3]);
		buildFigure += ".ch-" + parts[8] + "-" + convertOldColorToNew("ch", parts[8], parts[9]);
		buildFigure += ".lg-" + parts[4] + "-" + convertOldColorToNew("lg", parts[4], parts[5]);
		buildFigure += ".sh-" + (parts[6] == 730 ? 3206 : parts[6]) + "-" + convertOldColorToNew("sh", parts[6], parts[7]);
		buildFigure += takeCareOfHats(Integer.valueOf(parts[0]), Integer.valueOf(convertOldColorToNew("hr", parts[0], parts[1])));
		
		return buildFigure;
	}
	
	private static String getOldColorFromFigureList(String iPart, int iSprite, int iColorIndex) {
		String oldFigureData = getFileAsString("imager/oldfiguredata.json");
		
		if (oldFigureData == null) {
			return null;
		}	
		
		// Too tired of JSON to explain nested loops, but basically it grabs the colors with the same ID as given index
		JSONObject colorsJSON = new JSONObject(oldFigureData);
		JSONObject gendersObject = colorsJSON.getJSONObject("genders");
		
		for (String genderKey : gendersObject.keySet()) {
			Object genderValue = gendersObject.get(genderKey);
			JSONArray gender = new JSONArray(String.valueOf(genderValue));
			
			for (Object partObject : gender) {
				JSONObject partType = new JSONObject(String.valueOf(partObject));
				
				for (String partTypeKey : partType.keySet()) {
					Object partTypeValue = partType.get(partTypeKey);
					
					if (partTypeKey.equals(iPart)) {
						JSONArray partArray = new JSONArray(String.valueOf(partTypeValue));
						
						for (int j = 0; j < partArray.length(); j++) {
							JSONArray dataArray = partArray.getJSONArray(j);
							
							for (int k = 0; k < dataArray.length(); k++) {
								JSONObject data = dataArray.getJSONObject(k);
								
								int spriteID = data.optInt("s");
								
								if (spriteID == iSprite) {
									String spriteColorsString = data.optString("c");
									JSONArray spriteColorsArray = new JSONArray(spriteColorsString);
									
									return String.valueOf(spriteColorsArray.get(iColorIndex -  1));
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private static String convertOldColorToNew(String iPart, int iSprite, int iColorIndex) {
		String newFigureData = getFileAsString("imager/newfiguredata.json");
		
		String oldColor = getOldColorFromFigureList(iPart, iSprite, iColorIndex);
		
		JSONObject paletteJSON = new JSONObject(newFigureData);
		JSONObject paletteObject = paletteJSON.getJSONObject("palette");
		
		for (String paletteKey : paletteObject.keySet()) {
			Object paletteValue = paletteObject.get(paletteKey);
			JSONObject palette = new JSONObject(String.valueOf(paletteValue));
			
			for (String subPaletteKey : palette.keySet()) {
				Object subPaletteValue = palette.get(subPaletteKey);
				JSONObject subPalette = new JSONObject(String.valueOf(subPaletteValue));
				
				String color = subPalette.optString("color");
				
				if (color.equals(oldColor)) {
					return subPaletteKey;
				}
			}
		}
		
		return null;
	}
	
	private static String takeCareOfHats(int spriteID, int colorID) {
		switch (spriteID) {
			// Reggae
			case 120:
				return ".ha-1001-0";
			// Cap
			case 525:
			case 140:
				return ".ha-1002-" + colorID;
			// Comfy beanie
			case 150:
			case 535:
				return ".ha-1003-" + colorID;
			//Fishing hat
			case 160:
			case 565:
				return ".ha-1004-" + colorID;
			// Bandana
			case 570:
				return ".ha-1005-" + colorID;
			// Xmas beanie
			case 585:
			case 175:
				return ".ha-1006-0";
			// Xmas rodolph
			case 580:
			case 176:
				return ".ha-1007-0";
			// Bunny
			case 590:
			case 177:
				return ".ha-1008-0";
			// Hard Hat
			case 595:
			case 178:
				return ".ha-1009-1321";
			// Boring beanie
			case 130:
				return ".ha-1010-" + colorID;
			// HC Beard hat
			case 801:
				return ".hr-829-" + colorID + ".fa-1201-62.ha-1011-" + colorID;
			// HC Beanie
			case 800:
			case 810:
				return ".ha-1012-" + colorID;
			// HC Cowboy Hat
			case 802:
			case 811:
				return ".ha-1013-" + colorID;
			default:
				return ".ha-0-" + colorID;
	    }
	}
	
	/**
	 * Independent file reader
	 */
	private static String getFileAsString(String route) {
		try {
			return new String(Files.readAllBytes(Paths.get(route)));
		} catch (IOException ex) {
			System.out.println(ex);
			
			return null;
		}
	}
}
