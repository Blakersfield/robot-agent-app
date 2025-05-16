package com.blakersfield.gameagentsystem.llm.model.node;

public interface Node<I,O> {
    void act();
    void setInput(I input);
    O getOutput();
    void setNext(Node<O,?> next);
    Node<O,?> next();
    void reset();
}
