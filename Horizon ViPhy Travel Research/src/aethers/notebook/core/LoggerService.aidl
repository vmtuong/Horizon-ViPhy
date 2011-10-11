package aethers.notebook.core;

interface LoggerService
{
    void configure();
    
    boolean isRunning();
    
    void start();
    
    void stop();
}