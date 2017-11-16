package com.kasry.dataModels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Automaton {
    State start;
    State finish;
    Set<State> finalStates;

    /**
     * The default constructor of the Automaton
     */
    public Automaton() {
    }

    /**
     * Automaton's constructor
     * @param start the starting State of the Automaton
     * @param finish the finish state of the Automaton
     */
    public Automaton(State start, State finish) {
        this.start = start;
        this.finish = finish;
    }

    /**
     * A function that returns the epsilon closure of an automaton, the graph is traversed as a DFS
     * @param start represents the start state, that we'll start from
     * @param visited represents the visited states
     * @return
     */
    public Map<String, Set<State>> getEpsilonClosure(State start, Set<String> visited) {
        if (visited.contains(start.getName())) {
            return null;
        } else {
            Map<String, Set<State>> epsilonClosure = new HashMap<>();
            Set<State> epsilonSet = new HashSet<>();
            //Add the current state for itself :p
            epsilonSet.add(start);
            visited.add(start.getName());
            //Add all transition reachable states
            for (Map.Entry<Character, State> entry : start.getTransitions().entrySet()) {
                Map<String, Set<State>> transClosure = getEpsilonClosure(entry.getValue(), visited);
                if (transClosure !=  null) {
                    epsilonClosure.putAll(transClosure);
                }
            }
            //Add and Verify all the epsilon reachable states
            for (State tmp :
                    start.getEpsilon()){
                //Reachable by epsilon -> add the state
                epsilonSet.add(tmp);
                //Recursive call, to build all the epsilon Closure map
                Map<String, Set<State>> subEpsiClosure = getEpsilonClosure(tmp, visited);
                if (subEpsiClosure !=  null) {
                    epsilonClosure.putAll(subEpsiClosure);
                }
            }
            epsilonClosure.put(start.getName(), epsilonSet);
            return epsilonClosure;
        }
    }

    /**
     * This function returns all the transitions present in the Automata
     * @param start represent the start state
     * @param visited represents the visited states
     * @return all the transitions in the automaton
     */
    public Set<Character> getAutomateTransitions(State start, Set<String> visited) {
        if (visited.contains(start.getName())) {
            return null;
        } else {
            Set<Character> automateTransitions = new HashSet<>();
            visited.add(start.getName());
            for (Map.Entry<Character, State> entry : start.getTransitions().entrySet()) {
                automateTransitions.add(entry.getKey());
                Set<Character> subAutTrans = getAutomateTransitions(entry.getValue(), visited);
                if (subAutTrans != null) {
                    automateTransitions.addAll(subAutTrans);
                }
            }
            //Add transitions from the epsilon reachable states
            for (State tmp :
                    start.getEpsilon()){
                Set<Character> subAutTrans = getAutomateTransitions(tmp, visited);
                if (subAutTrans != null) {
                    automateTransitions.addAll(subAutTrans);
                }
            }
            return automateTransitions;
        }
    }

    /**
     * This function, adds an epsilon transition from the finish to the start state
     * Used for * and +
     */
    public void addEpsilonFinishStart() {
        finish.addEpsilon(start);
    }

    public State getStart() {
        return start;
    }

    public void setStart(State start) {
        this.start = start;
    }

    public State getFinish() {
        return finish;
    }

    public void setFinish(State finish) {
        this.finish = finish;
    }

    public Set<State> getFinalStates() {
        return finalStates;
    }

    public void setFinalStates(Set<State> finalStates) {
        this.finalStates = finalStates;
    }

    @Override
    public String toString() {
        Set<String> visited = new HashSet<>();
        return "Automaton{" +
                "start=" + start.toString(visited) +
                ", finish=" + (finish != null?finish.toString(visited):"NULL ") +
                ", finishStates=" + finalStates +
                '}';
    }
}
