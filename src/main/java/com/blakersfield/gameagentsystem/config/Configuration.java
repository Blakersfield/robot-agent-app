package com.blakersfield.gameagentsystem.config;

public class Configuration {
    public static String OLLAMA_BASE_URL = "OLLAMA_BASE_URL";
    public static String DEFAULT_OLLAMA_BASE_URL = "http://localhost";

    public static String OLLAMA_MODEL = "OLLAMA_MODEL";
    public static String DEFAULT_OLLAMA_MODEL = "gemma3";

    public static String OPENAI_API_TOKEN = "OPENAI_API_TOKEN";
    public static String DEFAULT_OPENAI_API_TOKEN = "OPENAI_API_TOKEN";

    public static String OPENAI_MODEL = "OPENAI_MODEL";
    public static String DEFAULT_OPENAI_MODEL = "gpt-4";

    public static String ENCRYPTION_CHECK_KEY = "ENCRYPTION_CHECK_KEY";
    public static String ENCRYPTION_CHECK_TRUTH = "EUREKA";

    public static String LLM_PROVIDER = "LLM_PROVIDER";
    public static String DEFAULT_LLM_PROVIDER = "Ollama";

    public static String OLLAMA_PORT = "OLLAMA_PORT";
    public static String DEFAULT_OLLAMA_PORT = "11434";

    public static String EXPORT_PATH = "EXPORT_PATH";
    public static String DEFAULT_EXPORT_PATH = System.getProperty("user.home");

    public static String INTERFACE_PROMPT = "INTERFACE_PROMPT";
    public static String DEFAULT_INTERFACE_PROMPT = 
            "Let's play a game. I will provide a word, and you will respond with a word that begins with " + 
            "the last letter of the word you provided and so on, back and forth. During that time I may " + 
            "provide additional feedback and clarification of rules. In those cases I will specify what my " + 
            "word is after providing the information. \n" + 
            "My word is:";
}
