package Interface;

import Component.ThreadPool.*;


public interface MessageProcessor extends Processor
{
    void process(Object object) throws Exception;
}
