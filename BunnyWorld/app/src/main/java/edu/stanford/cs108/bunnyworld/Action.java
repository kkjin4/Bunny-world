package edu.stanford.cs108.bunnyworld;

public class Action {
    private String verb, modifier;

    public Action(String verb, String modifier) {
        this.verb = verb;
        this.modifier = modifier;
    }

    public String toString() {
        return verb + " " + modifier;
    }

    public String getVerb() {
        return verb;
    }

    public String getModifier() {
        return modifier;
    }
}
