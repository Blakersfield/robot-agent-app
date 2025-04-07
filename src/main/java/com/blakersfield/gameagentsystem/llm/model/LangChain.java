package com.blakersfield.gameagentsystem.llm.model;

/*
 * robot interaction interface:
 *      InputNode -> InputClassificationAgent -> RuleExtractionAgent
 *                                            -> GameActionAgent 
 */
public interface LangChain<I> {
    public void run(Object input) ;
    public I output(); //need to enforce output with last node or cast
}
