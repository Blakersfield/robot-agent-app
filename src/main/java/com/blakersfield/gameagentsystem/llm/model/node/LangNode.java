package com.blakersfield.gameagentsystem.llm.model.node;

public interface LangNode<I, O> {
    public void act();
    public LangNode<?,?> next();
    public void setInput(I input);
    public O getOutput();
}
