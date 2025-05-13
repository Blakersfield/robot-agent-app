package com.blakersfield.gameagentsystem.llm.model.node;
public class NodeChainBuilder<I, O> {
    private Node<I, ?> firstNode;
    private Node<?, O> lastNode;
    private O lastOutput;

    public static <I, O> NodeChainBuilder<I, O> create() {
        return new NodeChainBuilder<>();
    }

    @SuppressWarnings("unchecked")
    public <T> NodeChainBuilder<I, T> add(Node<O, T> node) {
        NodeChainBuilder<I, T> newBuilder = new NodeChainBuilder<>();
        newBuilder.firstNode = this.firstNode;
        newBuilder.lastNode = node;
        newBuilder.lastOutput = null;
        
        if (firstNode == null) {
            newBuilder.firstNode = (Node<I, ?>) node;
        } else {
            ((Node<Object, O>) lastNode).setNext(node);
        }
        return newBuilder;
    }

    public Node<I, ?> build() {
        return firstNode;
    }

    public Node<?, O> getLastNode() {
        return lastNode;
    }

    public void execute(I input) {
        if (firstNode == null) {
            throw new IllegalStateException("No nodes in chain");
        }
        this.resetChain();
        firstNode.setInput(input);
        firstNode.act();
        
        this.lastOutput = lastNode.getOutput(); // store the last node's output after execution
    }

    private void resetChain() {
        Node<?, ?> current = firstNode;
        while (current != null) {
            current.reset();
            current = current.next();
        }
    }
    
    /**
     * Returns the output from the last node in the chain after execution.
     * @return The final output value from the chain execution
     * @throws IllegalStateException if the chain hasn't been executed yet
     */
    public O getLastOutput() {
        if (lastOutput == null) {
            throw new IllegalStateException("Chain has not been executed yet");
        }
        return lastOutput;
    }
} 