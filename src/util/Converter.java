package util;

import java.io.*;

/**
 * 常用格式转换器。
 */
public class Converter
{
    public static <T extends Serializable> byte[] objectToByteArray(T Object) throws IOException
    {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(Object);
        objOut.close();
        return byteOut.toByteArray();
    }

    public static Object byteArrayToObject(byte[] byteArray) throws IOException, ClassNotFoundException
    {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
             ObjectInputStream objIn = new ObjectInputStream(byteIn))
        {
            return objIn.readObject();
        }
    }
}
