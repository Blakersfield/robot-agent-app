package com.blakersfield.gameagentsystem.llm.model.node;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeChainBuilder<I, O> {
    private static final Logger logger = LoggerFactory.getLogger(NodeChainBuilder.class);
    private Node<I, ?> firstNode;
    private List<Node<?, O>> lastNodes = new ArrayList<>();
    private O lastOutput;

    public static <I, O> NodeChainBuilder<I, O> create() {
        return new NodeChainBuilder<>();
    }

    @SuppressWarnings("unchecked")
    public <T> NodeChainBuilder<I, T> add(Node<O, T> node) {
        NodeChainBuilder<I, T> newBuilder = new NodeChainBuilder<>();
        newBuilder.firstNode = this.firstNode;
        newBuilder.lastNodes = List.of((Node<?, T>) node);
        newBuilder.lastOutput = null;
        
        if (firstNode == null) {
            newBuilder.firstNode = (Node<I, ?>) node;
        } else {
            for (Node<?, O> lastNode : lastNodes) {
                ((Node<Object, O>) lastNode).setNext(node);
            }
        }
        return newBuilder;
    }

    public NodeChainBuilder<I, O> connect(Node<?, O> from, Node<O, ?> to) {
        from.setNext(to);
        return this;
    }

    public Node<I, ?> build() {
        return firstNode;
    }

    public List<Node<?, O>> getLastNodes() {
        return lastNodes;
    }

    public void execute(I input) {
        if (firstNode == null) {
            throw new IllegalStateException("No nodes in chain");
        }
        this.resetChain();
        firstNode.setInput(input);
        firstNode.act();
        
        Node<?, ?> current = firstNode;
        while (current.next() != null) {
            current = current.next();
        }
        this.lastOutput = (O) current.getOutput();
    }

    private void resetChain() {
        Node<?, ?> current = firstNode;
        while (current != null) {
            current.reset();
            current = current.next();
        }
    }
    
    public O getLastOutput() {
        if (lastOutput == null) {
            throw new IllegalStateException("Chain has not been executed yet");
        }
        return lastOutput;
    }
} 