package com.blakersfield.gameagentsystem.config;

public class Configuration {
    public static String OLLAMA_BASE_URL = "OLLAMA_BASE_URL";
    public static String DEFAULT_OLLAMA_BASE_URL = "http://localhost";

    public static String OLLAMA_MODEL = "OLLAMA_MODEL";
    public static String DEFAULT_OLLAMA_MODEL = "gemma3";

    public static String OPENAI_API_KEY = "OPENAI_API_KEY";
    public static String DEFAULT_OPENAI_API_KEY = "OPENAI_API_KEY";

    public static String OPENAI_API_SECRET = "OPENAI_API_SECRET";
    public static String DEFAULT_OPENAI_API_SECRET = "OPENAI_API_SECRET";

    public static String OPENAI_MODEL = "OPENAI_MODEL";
    public static String DEFAULT_OPENAI_MODEL = "gpt-4";

    public static String ENCRYPTION_CHECK_KEY = "ENCRYPTION_CHECK_KEY";
    public static String ENCRYPTION_CHECK_TRUTH = "EUREKA";

    public static String LLM_PROVIDER = "LLM_PROVIDER";
    public static String DEFAULT_LLM_PROVIDER = "Ollama";

    public static String OLLAMA_PORT = "OLLAMA_PORT";
    public static String DEFAULT_OLLAMA_PORT = "11434";
}
