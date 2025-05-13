package com.blakersfield.gameagentsystem.llm.model.node;

public abstract class BaseNode<I, O> implements Node<I, O> {
    protected I input;
    protected O output;
    protected Node<O, ?> next;

    @Override
    public void setNext(Node<O, ?> next) {
        this.next = next;
    }

    @Override
    public void setInput(I input) {
        this.input = input;
    }

    @Override
    public O getOutput() {
        return output;
    }

    @Override
    public Node<O, ?> next() {
        return next;
    }

    @Override
    public void reset() {
        this.input = null;
        this.output = null;
    }

    protected void propagateOutput() {
        if (next != null) {
            next.setInput(this.getOutput());
            next.act();
        }
    }
} 
