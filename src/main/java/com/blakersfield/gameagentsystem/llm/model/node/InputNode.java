package com.blakersfield.gameagentsystem.llm.model.node;

public class InputNode<I, O> extends BaseNode<I, O> {
    @SuppressWarnings("unchecked")
    @Override
    public void act() {
        this.output = (O) this.input;
        propagateOutput();
    }
}