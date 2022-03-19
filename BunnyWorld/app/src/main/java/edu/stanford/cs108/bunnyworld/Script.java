package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Script {
    List<Action> click, enter;
    Map<String, List<Action>> drop;

    public Script() {
        click = new ArrayList<>();
        enter = new ArrayList<>();
        drop = new HashMap<>();
    }

    public List<Action> getClickAction() {
        return click;
    }

    public List<Action> getEnterAction() {
        return enter;
    }

    public Map<String, List<Action>> getDropActions() {
        return drop;
    }

    public void add(String trigger, String shapeName, String verb, String objectName) {
        Action currAction = new Action(verb, objectName);

        switch(trigger) {
            case "onClick":
                click.add(currAction);
                break;
            case "onEnter":
                enter.add(currAction);
                break;
            case "onDrop":
                if (!drop.containsKey(shapeName)) {
                    drop.put(shapeName, new ArrayList<>());
                }
                drop.get(shapeName).add(currAction);
                break;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();


        for (int i = 0; i < click.size(); i++) {
            if (i == 0) {
                sb.append("on click ");
            }
            sb.append(click.get(i).toString());
            if (i < click.size() - 1) {
                sb.append(" ");
            }
        }
        sb.append(";");

        for (int i = 0; i < enter.size(); i++) {
            if (i == 0) {
                sb.append("on enter ");
            }
            sb.append(enter.get(i).toString());
            if (i < enter.size() - 1) {
                sb.append(" ");
            }
        }
        sb.append(";");


        for (String shape : drop.keySet()) {
            sb.append("on drop " + shape + " ");
            for (int i = 0; i < drop.get(shape).size(); i++) {
                sb.append(drop.get(shape).get(i).toString());
                if (i < drop.get(shape).size() - 1) {
                    sb.append(" ");
                }
            }
            sb.append(";");

        }
        return sb.toString();
    }

    static public Script fromString(String clause) {
        Script script = new Script();
        if (clause == null || clause.length() == 0) return script;
        String[] array = clause.split(";");
        for (String s : array) {
            String[] tokens = s.split(" ");
            if (tokens.length < 4) continue;
            int i = 2;
            switch(tokens[1]) {
                case "click":
                    while (i < tokens.length) {
                        script.add("onClick", null, tokens[i], tokens[i+1]);
                        i += 2;
                    }
                    break;
                case "enter":
                    while (i < tokens.length) {
                        script.add("onEnter", null, tokens[i], tokens[i+1]);
                        i += 2;
                    }
                    break;
                case "drop":
                    String shapeName = tokens[i];
                    i++;
                    while (i < tokens.length) {
                        script.add("onDrop", shapeName, tokens[i], tokens[i+1]);
                        i += 2;
                    }
                    break;
            }
        }
        return script;
    }
}
