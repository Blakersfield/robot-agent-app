package com.blakersfield.gameagentsystem.llm.model;

import com.blakersfield.gameagentsystem.llm.model.node.LangNode;

public class ChatChain implements LangChain{
    private LangNode<?, ?> head;

    public ChatChain(LangNode<?, ?> head) {
        this.head = head;
    }

    public void run(Object input) {
        LangNode current = head;
        current.setInput(input);
        while (current != null) {
            current.act();
            Object output = current.getOutput();
            current = current.getNextAgent();
            if (current != null) {
                current.setInput(output);
            }
        }
    }

    @Override
    public Object output() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'output'");
    }
}
