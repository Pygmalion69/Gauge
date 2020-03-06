package de.nitri.gauge;

public interface IGaugeNick {
    boolean shouldDrawMajorNick(int nick, float value);
    boolean shouldDrawHalfNick(int nick, float value);
    String getLabelString(int nick, float value);
}
