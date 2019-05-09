package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.WeakHashMap;

import static jdk.nashorn.internal.objects.NativeRegExp.source;

public class ReadClasspathFile {

    private static WeakHashMap<String,String> map = new WeakHashMap<>();

    public static String read(String classPath) throws IOException {
        String s = map.get(classPath);
        if (s!=null){
            return s;
        }
        //read
        ClassPathResource resource = new ClassPathResource(classPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(),"UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine())!=null){
            builder.append(line+"\n");
        }
        //DCL双检查锁
        if (!map.containsKey(classPath)){
            synchronized (ReadClasspathFile.class){
                if (!map.containsKey(classPath)){
                    map.put(classPath,builder.toString());
                }
            }
        }
        return builder.toString();
    }
}
