package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.model.node.LangNode;

public abstract class Agent<I, O> implements LangNode<I, O> {
    protected I input;
    protected O output;

    @Override
    public abstract LangNode<?, ?> next(); 

    @Override
    public void setInput(I input) {
        this.input = input;
    }

    @Override
    public O getOutput() {
        return output;
    }

    @Override
    public abstract void act(); // transform input, do RAG, set output, etc., whatever
}