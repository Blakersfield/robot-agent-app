package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.model.node.Node;

public abstract class Agent<I,O> implements Node<I,O> {
    protected I input;
    protected O output;
    protected Node<O,?> next;

    @Override
    public void setNext(Node<O,?> next){
        this.next = next;
    }
    @Override
    public abstract void act();

    @Override
    public void setInput(I input) {
        this.input = input;
    }

    protected void setOutput(O output){
        this.output = output;
    }

    @Override
    public O getOutput() {
        return output;
    }

    @Override
    public Node<O, ?> next() {
        return this.next;
    }
}
