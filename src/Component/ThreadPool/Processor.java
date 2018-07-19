package Component.ThreadPool;

public interface Processor
{
    /**
     * 对特定对象进行处理的函数。传入参数根据不同需求自行进行强制格式转换。
     *
     * @param object 要被处理的对象。
     */
    void process(Object object) throws Exception;
}
