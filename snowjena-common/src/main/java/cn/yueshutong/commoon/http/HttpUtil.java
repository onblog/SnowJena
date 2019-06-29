package cn.yueshutong.commoon.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装HttpURLConnection开箱即用
 * Create by yster@foxmail.com 2018/9/10/010 19:17
 */
public class HttpUtil {
    private HttpURLConnection connection;
    private Charset charset = Charset.forName("UTF-8");
    private int readTimeout = 32000;
    private int connectTimeout = 10000;
    private String method = "GET";
    private boolean doInput = true;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> data = new HashMap<>();

    /**
     * 实例化对象
     */
    public static HttpUtil connect(String url) throws IOException {
        return new HttpUtil((HttpURLConnection) new URL(url).openConnection());
    }

    /**
     * 禁止new实例
     */
    private HttpUtil() {
    }

    private HttpUtil(HttpURLConnection connection) {
        this.connection = connection;
    }

    /**
     * 设置读去超时时间/ms
     */
    public HttpUtil setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * 设置链接超时时间/ms
     */
    public HttpUtil setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    /**
     * 设置请求方式
     */
    public HttpUtil setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * 添加Headers
     */
    public HttpUtil setHeaders(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * 是否接受输入流
     * 默认true
     */
    public HttpUtil setDoInput(boolean is) {
        this.doInput = is;
        return this;
    }

    /**
     * 设置请求响应的编码
     */
    public HttpUtil setCharset(String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    /**
     * 写入参数,不支持GET方式，GET参数需自行在URL追加
     * POST的参数:demo=1&name=2
     */
    public HttpUtil setData(String key,String value) {
        data.put(key,value);
        return this;
    }

    /**
     * 发起请求
     */
    public HttpUtil execute() throws IOException {
        //直接关闭链接
        headers.put("Connection", "close");
        //添加请求头
        for (String key : headers.keySet()) {
            connection.setRequestProperty(key, headers.get(key));
        }
        //设置读去超时时间为10秒
        connection.setReadTimeout(readTimeout);
        //设置链接超时为10秒
        connection.setConnectTimeout(connectTimeout);
        //设置请求方式,GET,POST
        connection.setRequestMethod(method.toUpperCase());
        //接受输入流
        connection.setDoInput(doInput);
        //写入参数
        if (!data.isEmpty() & !method.equalsIgnoreCase("GET")) {
            //启动输出流，当需要传递参数时需要开启
            connection.setDoOutput(true);
            //添加请求参数，注意：如果是GET请求，参数要写在URL中
            OutputStream output = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, charset));
            //写入参数 用&分割
            writer.write(getDataString());
            writer.flush();
            writer.close();
        }
        //发起请求
        connection.connect();
        return this;
    }

    private String getDataString() {
        StringBuilder builder = new StringBuilder();
        List<Map.Entry<String, String>> list = new ArrayList<>(data.entrySet());
        for (int i = 0; i< list.size(); i++) {
            Map.Entry<String, String> entry = list.get(i);
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            if (i<list.size()-1){
                builder.append("&");
            }
        }
        return builder.toString();
    }

    /**
     * 获取HttpURLConnection
     */
    public HttpURLConnection getConnection() {
        return this.connection;
    }

    /**
     * 获取响应字符串
     */
    public String getBody(String... charsets) {
        //设置编码
        String charset = "UTF-8";
        if (charsets.length > 0) {
            charset = charsets[0];
        }
        //读取输入流
        try {
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
            String line = bufferedReader.readLine();
            StringBuilder builder = new StringBuilder();
            while (line != null) {
                builder.append(line);
                line = bufferedReader.readLine();
            }
            return builder.toString();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        //失败返回NULL
        return null;
    }

    public static void main(String[] args) throws IOException {
        String body = HttpUtil.connect("http://www.baidu.com")
                .setMethod("GET")
                .execute()
                .getBody();
        System.out.println(body);
    }

}
