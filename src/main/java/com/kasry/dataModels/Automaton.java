package com.kasry.dataModels;

public class Automaton {
    State start;
    State finish;

    public Automaton() {
    }

    /**
     * This function, adds an epsilon transition from the finish to the start state
     * Used for * and +
     */
    public void addEpsilonFinishStart() {
        finish.addEpsilon(start);
    }

    /*public static Automaton newTrivialAutomaton(State sf) {
        return new Automaton(sf, sf)
    }*/

    public Automaton(State start, State finish) {
        this.start = start;
        this.finish = finish;
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

    @Override
    public String toString() {
        return "Automaton{" +
                "start=" + start +
                ", finish=" + finish +
                '}';
    }
}
