/**
 * Utilities for general use.
 * 
 * Copyright (C) 2018 Lightning Creations
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lightning.firewood.util;

/**
 * @author Ray Redondo
 *
 */
public class Util {
	public static double levenshteinDistance(String a, String b) {
		int lengthA = a.length(), lengthB = b.length();
		double[][] distMatrix = new double[lengthA+1][lengthB+1];
		
		for(int i = 0; i < lengthA+1; i++) distMatrix[i][0] = i;
		for(int j = 0; j < lengthB+1; j++) distMatrix[0][j] = j;
		
		for(int j = 0; j < lengthB; j++) {
			for(int i = 0; i < lengthA; i++) {
				int cost = Math.abs(a.substring(i, i+1).toLowerCase().compareTo(b.substring(j, j+1).toLowerCase())); // Ouch.
				double cI = distMatrix[i][j+1] + 1;
				double cD = distMatrix[i+1][j] + 1;
				double cS = distMatrix[i][j] + cost;
				if(cI <= cD) {
					if(cI <= cS) distMatrix[i+1][j+1] = cI; else distMatrix[i+1][j+1] = cS;
				} else {
					if(cD <= cS) distMatrix[i+1][j+1] = cD; else distMatrix[i+1][j+1] = cS;
				}
			}
		}
		
		return distMatrix[lengthA][lengthB];
	}
	
	public static double valuePhrase(String a, String b) {
		return levenshteinDistance(a, b);
	}

	public static double valueWords(String a, String b) {
		String[] wordsA = a.split(" |_|-"), wordsB = b.split(" |_|-");
		double thisD, wordbest;
		double wordsTotal = 0;
		for(int word1 = 0; word1 < wordsA.length; word1++) {
			wordbest = b.length();
			for(int word2 = 0; word2 < wordsB.length; word2++) {
				thisD = levenshteinDistance(wordsA[word1], wordsB[word2]);
				if(thisD < wordbest) wordbest = thisD;
				if(thisD == 0) break;
			}
        	wordsTotal += wordbest;
		}
    	return wordsTotal;
	}
	
	public static double wordDifference(String a, String b) {
		double phrase = valuePhrase(a,b)-0.8*Math.abs(b.length()-a.length());
		double words = valueWords(a,b);
		return Math.max(phrase, words)*0.2+Math.min(phrase, words);
	}
	
	public static int findClosestString(String[] list, String wanted) {
		if(list.length == 0) return -1;
		
		int bestIndex = 0;
		double bestDiff = wordDifference(list[0], wanted);
		for(int i = 1; i < list.length; i++) {
			double diff = wordDifference(list[i], wanted);
			if(diff < bestDiff) {
				bestDiff = diff;
				bestIndex = i;
			}
		}
		
		return bestIndex;
	}
}