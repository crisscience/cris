/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

/**
 *
 * @author jiaxu
 */
public class BalancedMatcher {
    String[] patternBegin;
    String[] patternEnd;
    String source;
    Integer start;
    Integer end;
    Integer nextPosition;

    public BalancedMatcher() {
        this.start = 0;
        this.end = null;
        this.nextPosition = 0;
    }

    public BalancedMatcher(String[] patternBegin, String[] patternEnd) {
        this.start = 0;
        this.end = null;
        this.nextPosition = 0;
        this.patternBegin = patternBegin;
        this.patternEnd = patternEnd;
    }

    public void compile(String source) {
        this.start = 0;
        this.end = null;
        this.nextPosition = 0;
        this.source = source;
    }

    public boolean find() {
        if (start == -1 || source == null || source.isEmpty()) {
            return false;
        }

        // figure out the matching end
        int count = 0;
        boolean found = false;
        int i = nextPosition;
        while (i < source.length()) {
            int skip = 1;

            boolean s = false;
            for (int j = 0; j < patternBegin.length; j++) {
                s = source.startsWith(patternBegin[j], i);
                if (s) {
                    if (!found) {
                        // reset the count when we found the first match
                        count = 0;
                    }
                    found = true;
                    skip = patternBegin[j].length();
                    break;
                }
            }

            if (found && !s) {
                s = source.startsWith("{", i);
            }

            boolean e = false;
            for (int j = 0; j < patternEnd.length; j++) {
                e = source.startsWith(patternEnd[j], i);
                if (e) {
                    break;
                }
            }

            if (s) {
                if (count == 0) {
                    start = i;
                }
                count++;
            } else if (e) {
                count--;
                if (count == 0) {
                    end = i + 1;
                    nextPosition = end;
                    return true;
                }
            }

            i += skip;
        }

        return false;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}
