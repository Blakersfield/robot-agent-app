package com.blakersfield.gameagentsystem.llm.model.node.agent.data;
import java.util.List;

public class RuleComparisonResult {
    private List<Rule> newRules;
    private List<Rule> duplicateRules;
    private List<RuleModification> modifiedRules;
    public List<Rule> getNewRules() {
        return newRules;
    }
    public void setNewRules(List<Rule> newRules) {
        this.newRules = newRules;
    }
    public List<Rule> getDuplicateRules() {
        return duplicateRules;
    }
    public void setDuplicateRules(List<Rule> duplicateRules) {
        this.duplicateRules = duplicateRules;
    }
    public List<RuleModification> getModifiedRules() {
        return modifiedRules;
    }
    public void setModifiedRules(List<RuleModification> modifiedRules) {
        this.modifiedRules = modifiedRules;
    }

    
}