package com.blakersfield.gameagentsystem.llm.model;

public class LangChainNode {
    private int nodeId;
    private int langChainId;
    private Integer nextNodeId;

    public LangChainNode(int nodeId, int langChainId, Integer nextNodeId) {
        this.nodeId = nodeId;
        this.langChainId = langChainId;
        this.nextNodeId = nextNodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getLangChainId() {
        return langChainId;
    }

    public Integer getNextNodeId() {
        return nextNodeId;
    }
}
