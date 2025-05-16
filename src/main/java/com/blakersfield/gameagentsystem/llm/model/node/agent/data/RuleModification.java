package com.blakersfield.gameagentsystem.llm.model.node.agent.data;

public class RuleModification {
    private Rule original;
    private Rule modified;
    public Rule getOriginal() {
        return original;
    }
    public void setOriginal(Rule original) {
        this.original = original;
    }
    public Rule getModified() {
        return modified;
    }
    public void setModified(Rule modified) {
        this.modified = modified;
    }

    
}
