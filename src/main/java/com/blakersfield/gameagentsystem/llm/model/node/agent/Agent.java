package com.blakersfield.gameagentsystem.llm.model.node.agent;

import com.blakersfield.gameagentsystem.llm.model.node.BaseNode;
import com.blakersfield.gameagentsystem.llm.model.node.Node;

public abstract class Agent<I,O> extends BaseNode<I,O> {
    @Override
    public abstract void act();
}
