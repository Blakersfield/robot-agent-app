package com.blakersfield.gameagentsystem.llm.model.node;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputNode<I, O> extends BaseNode<I, O> {
    private static final Logger logger = LoggerFactory.getLogger(InputNode.class);

    @SuppressWarnings("unchecked")
    @Override
    public void act() {
        if (input instanceof List) {
            logger.debug("Received input with {} items", ((List<?>) input).size());
        } else {
            logger.debug("Received input: {}", input);
        }
        this.output = (O) this.input;
        propagateOutput();
    }
}