package com.blakersfield.gameagentsystem.llm.model.node;

public class InputNode implements LangNode<String, String> {
    private LangNode<String, ?> next;
    private String input;

    public InputNode(LangNode<String, ?> next) {
        this.next = next;
    }

    @Override
    public void act() {
        if (next != null) {
            next.setInput(input);
        }
    }

    @Override
    public LangNode<String, ?> next() {
        return next;
    }

    @Override
    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public String getOutput() {
        return input;
    }
}