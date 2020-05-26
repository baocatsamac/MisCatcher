package helper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHelper {

    static Logger logger;
    static FileHandler fileHandler;

    private static void setupLog(){
        try {
            if(logger == null){
                logger = Logger.getLogger("MyDebuggingLog");
                logger.info("Welcome to Debugging Log!");
            }
            if (fileHandler == null){
                // This block configure the logger with handler and formatter
                fileHandler = new FileHandler("C:/temp/test/MyDebugging.log");
                logger.addHandler(fileHandler);
                SimpleFormatter formatter = new SimpleFormatter();
                fileHandler.setFormatter(formatter);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logInfo(String message){
        // Setup local log file for Debugging
        setupLog();

        // log debugging or error message
        logger.info(message);
    }
}
