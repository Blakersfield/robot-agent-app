# Game Agent Rule Inference
CSUCI MSCS project Spring 2025
A Java Swing-based application that provides an interface for interacting with Large Language Models (LLMs) through a chat interface and game action processing system.
The application uses SQLite as an embedded data store, HttpCore5 for web requests and jackson databind for serialization/deserialization.
The purpose of this app is to serve as a basis for research into the capabilities of LLM's in infering information such as game rules based on interaction.

## Prerequisites

- Java JDK 17 or higher
- Maven 3.6 or higher
- SQLite (included as a dependency)
- Ollama (optional, for local LLM support)
- OpenAI API key (optional, for OpenAI integration)

## Building the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd GameAgentSystem
```

2. Build using Maven:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn exec:java -Dexec.mainClass="com.blakersfield.gameagentsystem.Main"
```
Alternatively, use the maven plugin and run configuration of your IDE to build and run.

## Dependencies

The application uses the following key dependencies (managed by Maven):

- `org.apache.httpcomponents:httpclient` - For HTTP requests
- `com.fasterxml.jackson.core:jackson-databind` - For JSON processing
- `org.slf4j:slf4j-api` - For logging
- `org.xerial:sqlite-jdbc` - For SQLite database support
- `com.formdev:flatlaf` - For modern UI look and feel

## Initial Setup

When you first run the application, you'll be prompted to:

1. Set up an admin password
   - Password must be alphanumeric
   - This password will be used to access settings and encrypt sensitive data

2. Configure LLM Provider in the settings menu
   - Choose between Ollama (local) or OpenAI
   - Ollama default settings are matched to the default configuration of running ```ollama serve ``` locally

## Application Features

### Chat Interface

The main chat interface allows you to:
- Interact with the selected LLM to ensure operation
- Send messages to the LLM
- View conversation history
- Create new chat sessions
- Rename chat sessions
- View chat logs

### Interface Panel

The interface panel provides:
- Game action processing with agentic flow for experimentation
- Rule extraction capabilities
- Specialized LLM interactions for game-related tasks

### Settings Panel

Access the settings panel to configure:

#### LLM Provider Settings

1. Ollama Settings (for local LLM):
   - Base URL (default: http://localhost)
   - Model selection (gemma3, llama3:8b, mistral, phi3, gemma:7b, or custom)
   - Port (default: 11434)

2. OpenAI Settings:
   - API Bearer token
   - Model selection (gpt-4, gpt-3.5-turbo, gpt-4-turbo)
  
3. General:
   - Game Interface prompt
   - Database panel with export, data clearing and sql console capabilites 

### Flow Panel

Currently a placeholder for future flowchart builder functionality.

## Security Features

- Password-protected settings access
- Encrypted storage of sensitive data
- Secure handling of API keys

## Logging

The application maintains detailed logs:
- Chat panel logs: `logs/chat-panel.log`
- Interface panel logs: `logs/interface-panel.log`
- Main application logs: `logs/application.log`

Logs can be viewed through the UI using the "View Logs" button in each panel.

## Database

The application uses SQLite for data persistence:
- Chat messages
- Configuration settings
- Game rules
- Language chain nodes
- Agent configurations

The database file is stored as `app.db` in the application directory.

## Troubleshooting

1. If the application fails to start:
   - Check Java version: `java -version`
   - Verify Maven installation: `mvn -version`
   - Check database permissions
   - Verify log files are writable
   - if all else fails, delete app.db and reinitiate app storage

2. If LLM integration fails:
   - For Ollama: Ensure Ollama is running locally
   - For OpenAI: Verify API key and secret are correct
   - Check network connectivity
   - Review application logs
## Areas for future work and research
   - Add flexible agent chain configuration
   - Add interaction and robot/ROS specific agent and client (websocket)
   - Add interface with voice capability
   - Add usage of vector similarity for semantic comparison tasks
## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

 GPL-3.0 license
