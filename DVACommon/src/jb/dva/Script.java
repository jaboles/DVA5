//
//  Script.java
//  DVA
//
//  Created by Jonathan Boles on 29/05/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package jb.dva;
import java.net.URL;
import java.util.ArrayList;

// Represents a DVA announcement, and handles translation into a list of sounds.
public class Script {
    String defaultVoice;
    String script;
    String lscript;
    String name;
    StringBuffer canonical;
    ArrayList<URL> translatedData;
    ArrayList<Integer> translatedDataOffsets;

    private static final char[] punctuation = { '.', ',', ':', ';' };
    private static final char[] whitespace = { ' ', '\r', '\n', '\t' };

    public Script(String voice, String script) {
        this.defaultVoice = voice;
        this.script = script;
        this.lscript = script.toLowerCase();
    }

    public Script(String name, String voice, String script) {
        this.name = name;
        this.defaultVoice = voice;
        this.script = script;
        this.lscript = script.toLowerCase();
    }

    public int hashCode()
    {
        return script.hashCode();
    }

    public boolean equals(Object other)
    {
        return (other instanceof Script) &&
                name.equals(((Script)other).name) &&
                defaultVoice.equals(((Script)other).defaultVoice);
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String s)
    {
        this.name = s;
    }

    public void setScript(String s) {
        this.script = s;
        this.lscript = s.toLowerCase();
        this.translatedData = null;
    }

    public void setVoice(String s) {
        this.defaultVoice = s;
        this.translatedData = null;
    }

    public String getVoice() {
        return defaultVoice;
    }

    public String getScript() {
        return script;
    }

    public String toString()
    {
        return "<html><body><span style=\"font-size: 105%\">" + this.name + "</span><br><i><span style=\"font-size: 90%\">" + this.script + "</span></i></body></html>";
    }

    public ArrayList<URL> getTranslatedUrlList(SoundLibrary library) throws Exception {
        if (translatedData == null) {
            // Requires translating
            translate(library);
        }
        return translatedData;
    }

    public String getCanonicalScript(SoundLibrary library) throws Exception {
        translate(library);
        return canonical.toString();
    }

    private void translate(SoundLibrary library) throws Exception {
        translatedData = new ArrayList<>();
        translatedDataOffsets = new ArrayList<>();
        canonical = new StringBuffer(10240);
        int pos = 0;
        int length = script.length();

        String s = accumulateWhitespace(pos);
        canonical.append(s);
        pos += s.length();

        String lastToken = null;
        int preferredInflection = SoundInflection.NONE;
        while (pos < length) {
            String token = findNextToken(pos, library);
            if (token.length() == 1 && isPunctuation(token.charAt(0))) {
                if (token.charAt(0) == ',') {
                    translatedData.add(Script.class.getResource("/silence50msec.wav"));
                } else {
                    translatedData.add(Script.class.getResource("/silence200msec.wav"));
                }
                canonical.append(token);
            } else if (token.length() == 1 && token.charAt(0) == '`') {
                preferredInflection = SoundInflection.ENDING;
                canonical.append(token);
            } else if (token.length() == 1 && token.charAt(0) == '^') {
                preferredInflection = SoundInflection.BEGINNING;
                canonical.append(token);
            } else {
                SoundReference ref = library.get(token);
                if (preferredInflection == SoundInflection.NONE) {
                    // Peek at the next token
                    int nextTokenPos = pos + token.length() + accumulateWhitespace(pos + token.length()).length();
                    String nextToken = (nextTokenPos < length) ? findNextToken(nextTokenPos, library) : null;
                    if (nextToken != null && nextToken.equals(",")) {
                        preferredInflection = SoundInflection.REGULAR;
                    } else if (nextToken != null && nextToken.equals(".")) {
                        preferredInflection = SoundInflection.ENDING;
                    } else if (lastToken == null || lastToken.equals(".")) {
                        preferredInflection = SoundInflection.BEGINNING;
                    } else if (nextToken == null) {
                        preferredInflection = SoundInflection.REGULAR;
                    }
                }
                URL u = null;
                if (preferredInflection == SoundInflection.BEGINNING && ref.beginning != null) {
                    u = ref.beginning;
                } else if (preferredInflection == SoundInflection.REGULAR && ref.regular != null) {
                    u = ref.regular;
                } else if (preferredInflection == SoundInflection.ENDING && ref.ending != null) {
                    u = ref.ending;
                } else if (ref.regular != null) {
                    u = ref.regular;
                } else if (ref.ending != null) {
                    u = ref.ending;
                } else if (ref.beginning != null) {
                    u = ref.beginning;
                }
                preferredInflection = SoundInflection.NONE;

                if (u != null)
                {
                    translatedData.add(u);
                    translatedDataOffsets.add(pos);
                    canonical.append(library.getCanonicalName(token));
                }
            }

            pos += token.length();

            s = accumulateWhitespace(pos);
            canonical.append(s);
            pos += s.length();
            lastToken = token;
        }
    }

    public ArrayList<Integer> getTranslatedDataOffsets() {
        return translatedDataOffsets;
    }

    private String findNextToken(int pos, SoundLibrary library) throws Exception {

        // Return punctuation if at the start.
        if (isPunctuation(lscript.charAt(pos))) {
            return lscript.substring(pos, pos + 1);
        }
        else if (lscript.charAt(pos) == '`') {
            return lscript.substring(pos, pos + 1);
        }
        else if (lscript.charAt(pos) == '^') {
            return lscript.substring(pos, pos + 1);
        }

        int rpos = Math.min(lscript.length(), pos + library.getLongestSoundNameLength());
        String s = lscript.substring(pos, rpos);
        while (!library.contains(s)) {
            int lastIndexOfSpaceOrPunctuation = getLastIndexOfSpaceOrPunctuation(s);
            if (lastIndexOfSpaceOrPunctuation == -1) {
                translatedData = null;
                throw new Exception("An error occurred while attempting to translate. Library='" + library.getName() + "' Script='" + script + "'. Parse error at '" + script.substring(pos) + "'. Error at position "+pos);
            }
            s = s.substring(0, lastIndexOfSpaceOrPunctuation);
        }
        return s;
    }

    private String accumulateWhitespace(int currentPos) {
        int pos = currentPos;
        // consume whitespace and other stuff
        int length = script.length();
        while (pos < length && Character.isWhitespace(script.charAt(pos))) {
            pos++;
        }
        return script.substring(currentPos, pos);
    }

    // Returns whether the string is a single punctuation character.
    public static boolean isPunctuation(char c)
    {
        for (char p : punctuation)
        {
            if (c == p) return true;
        }

        return false;
    }

    // Finds the first index of the last grouping of spaces and punctuation characters.
    private int getLastIndexOfSpaceOrPunctuation(String s)
    {
        int last = -1;
        for (char w : whitespace)
        {
            int l = s.lastIndexOf(w);
            if (l >= 0 && l > last)
            {
                last = l;
            }
        }
        for (char p : punctuation)
        {
            int l = s.lastIndexOf(p);
            if (l >= 0 && l > last)
            {
                last = l;
            }
        }
        return last;
    }
}
