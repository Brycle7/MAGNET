package com.example.android.wifidirect.discovery;

/**
 * Created by Naser on 5/9/2016.
 */
public class SubStringBetween {
    public static String subStringBetween(String sentence, String before, String after) {

        int startSub = SubStringBetween.subStringStartIndex(sentence, before);
        int stopSub = SubStringBetween.subStringEndIndex(sentence, after);

        String newWord = sentence.substring(startSub, stopSub);
        return newWord;
    }

    public static int subStringStartIndex(String sentence, String delimiterBeforeWord) {

        int startIndex = 0;
        String newWord = "";
        int x = 0, y = 0;

        for (int i = 0; i < sentence.length(); i++) {
            newWord = "";

            if (sentence.charAt(i) == delimiterBeforeWord.charAt(0)) {
                startIndex = i;
                for (int j = 0; j < delimiterBeforeWord.length(); j++) {
                    try {
                        if (sentence.charAt(startIndex) == delimiterBeforeWord.charAt(j)) {
                            newWord = newWord + sentence.charAt(startIndex);
                        }
                        startIndex++;
                    } catch (Exception e) {
                    }

                }
                if (newWord.equals(delimiterBeforeWord)) {
                    x = startIndex;
                }
            }
        }
        return x;
    }

    public static int subStringEndIndex(String sentence, String delimiterAfterWord) {

        int startIndex = 0;
        String newWord = "";
        int x = 0;

        for (int i = 0; i < sentence.length(); i++) {
            newWord = "";

            if (sentence.charAt(i) == delimiterAfterWord.charAt(0)) {
                startIndex = i;
                for (int j = 0; j < delimiterAfterWord.length(); j++) {
                    try {
                        if (sentence.charAt(startIndex) == delimiterAfterWord.charAt(j)) {
                            newWord = newWord + sentence.charAt(startIndex);
                        }
                        startIndex++;
                    } catch (Exception e) {
                    }

                }
                if (newWord.equals(delimiterAfterWord)) {
                    x = startIndex;
                    x = x - delimiterAfterWord.length();
                }
            }
        }
        return x;
    }
}
