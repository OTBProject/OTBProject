package io.github.otbproject.otb.plugin.content;

public class DataRetrievalException extends RuntimeException {
    DataRetrievalException(ContentPlugin plugin) {
        super("Plugin '" + plugin + "' failed to initialize data or did so too slowly, so it cannot be retrieved");
    }

    DataRetrievalException(String message, Throwable cause) { super(message, cause); }

    DataRetrievalException(Throwable cause) { this("Exception during data initialization.", cause); }
}
