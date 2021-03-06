package io.datonis.sdk.communicator;

import io.datonis.sdk.message.Message;

/**
 * This interface represents a mechanism for the Datonis client to transfer data 
 * being generated by the agent
 * 
 * @author Ranjit Nair (rajesh@altizon.com)
 * @author Rajesh Jangam (rajesh@altizon.com)
 * 
 */
public interface EdgeCommunicator
{

    public static final int INVALID_PARAMS = -1;
    public static final int OK = 0;
    public static final int UNAUTHORIZED = 1;
    public static final int INVALID_REQUEST = 2;
    public static final int EXCESSIVE_RATE = 3;
    public static final int FAILED = 4;
    public static final int NOT_ACCEPTABLE = 5;
    public static final int NO_CONNECTION = 6;
    public static final int TIMED_OUT = 7;
    public static final int BAD_REQUEST = 8;
    public static final int INTERNAL_SERVER_ERROR = 9;

    /**
     * Connects to the Datonis server
     *
     * @return
     */
    public int connect();

    /**
     * Tells whether the communicator is connected
     */
    public boolean isConnected();
    
    /**
     * Transmits the specified message to Datonis
     *
     * @param msg
     * @return
     */
    public int transmit(Message msg);

    /**
     * Shuts down communication with the Datonis server
     *
     */
    public void shutdown();
}
