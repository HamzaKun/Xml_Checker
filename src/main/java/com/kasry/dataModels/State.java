package com.kasry.dataModels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class State {
    private static int num = 0;
    private String name;
    private Map<Character, State> transitions;
    private Set<State> epsilon;

    public State(){
        num+=1;
        name = Integer.toString(num);
        transitions = new HashMap<>();
        epsilon = new HashSet<>();
    }

    public State(Character character, State state) {
        num+=1;
        name = Integer.toString(num);
        transitions = new HashMap<Character, State>();
        transitions.put(character, state);
        epsilon = new HashSet<>();
    }

    /**
     * Adds an epsilon transition to the state in @param fState
     * @param fState represents the end state of the epsilon transition
     */
    public void addEpsilon(State fState) {
        epsilon.add(fState);
    }

    public void addTransition(Character symbol, State endState) {
        transitions.put(symbol, endState);
    }

    public void changeFinish(State finish) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Character, State> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<Character, State> transitions) {
        this.transitions = transitions;
    }

    public Set<State> getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(Set<State> epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public String toString() {
        return "name='" + name ;
    }
    //Using a to String method as DFS
    public String toString(Set<String> visited) {
        if (visited.contains(this.getName())) {
            return this.getName();
        } else {
            String str = "{" + "name: "+ this.getName()+ " Transitions: [";
            visited.add(this.getName());
            for (Map.Entry<Character, State> entry : transitions.entrySet()) {
                str = str.concat(Character.toString(entry.getKey()));
                String recString = null;
                if (entry.getValue() != null)
                    recString = entry.getValue().toString(visited);
                if (recString == null)
                    str = str.concat(": None ");
                else
                    str = str.concat(recString);
            }
            str = str.concat("], epsilon : [");
            for (State state :
                    this.epsilon) {
                String recString = state.toString(visited);
                str = str.concat(recString);
            }
            str = str.concat(" ]}");
            return str;
        }
    }
}
