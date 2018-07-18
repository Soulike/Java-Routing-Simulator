package Objects;

import java.io.Serializable;

/**
 * Pair 容器，用于存储成对的信息。
 *
 * @author soulike
 */
public class Pair<F, S> implements Serializable, Cloneable
{
    private F first;
    private S second;

    /**
     * 默认构造函数，将容器内容均设置为 null。
     */
    public Pair()
    {
        first = null;
        second = null;
    }

    /**
     * 构造函数，设置容器内容。
     *
     * @param first  容器装载的第一个对象。
     * @param second 容器装载的第二个对象。
     */
    public Pair(F first, S second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * 获取容器的第一个对象。
     */
    public F getFirst()
    {
        return first;
    }

    /**
     * 获取容器的第二个对象。
     */
    public S getSecond()
    {
        return second;
    }

    /**
     * 设置容器的第一个对象。
     *
     * @param first 要设置为容器第一个对象的对象。
     */
    public void setFirst(F first)
    {
        this.first = first;
    }

    /**
     * 设置容器的第二个对象。
     *
     * @param second 要设置为容器第二个对象的对象。
     */
    public void setSecond(S second)
    {
        this.second = second;
    }

    /**
     * 判断两个 Pair 容器中的内容是否相等。
     *
     * @param obj 要比较的对象。
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj.getClass() != Pair.class)
        {
            return false;
        }
        else
        {
            return first.equals(second) && second.equals(first);
        }
    }

    /**
     * hashCode 计算
     */
    @Override
    public int hashCode()
    {
        return first.hashCode() + second.hashCode();
    }

    /**
     * 输出为 String。格式为Pair(first, second)。
     */
    @Override
    public String toString()
    {
        return String.format("Pair(%s, %s)", first.toString(), second.toString());
    }

    /**
     * 复制对象。浅度复制容器中对象。
     */
    public Pair<F, S> clone()
    {
        return new Pair<>(first, second);
    }
}
