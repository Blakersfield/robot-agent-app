package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.model.node.LangNode;

public abstract class Agent<I, O> implements LangNode<I, O> {
    protected I input;
    protected O output;
    protected AgentState state = AgentState.IDLE;
    protected Exception lastError;

    public enum AgentState {
        IDLE,
        PROCESSING,
        COMPLETED,
        ERROR
    }

    @Override
    public abstract LangNode<?, ?> next(); 

    @Override
    public void setInput(I input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        this.input = input;
        this.state = AgentState.IDLE;
        this.lastError = null;
    }

    @Override
    public O getOutput() {
        if (state == AgentState.ERROR) {
            throw new IllegalStateException("Agent is in error state", lastError);
        }
        if (state != AgentState.COMPLETED) {
            throw new IllegalStateException("Agent has not completed processing");
        }
        return output;
    }

    @Override
    public abstract void act(); // transform input, do RAG, set output, etc., whatever

    public AgentState getState() {
        return state;
    }

    public Exception getLastError() {
        return lastError;
    }

    protected void setError(Exception error) {
        this.state = AgentState.ERROR;
        this.lastError = error;
    }

    protected void setCompleted() {
        this.state = AgentState.COMPLETED;
    }

    protected void setProcessing() {
        this.state = AgentState.PROCESSING;
    }
}