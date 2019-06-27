package cn.yueshutong.core.ticket;

import cn.yueshutong.commoon.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 发票服务器
 */
public class TicketServer {
    private Map<String, Integer> ip;

    private Logger logger = LoggerFactory.getLogger(TicketServer.class);
    private List<String> serverList = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    private int pos = 0;
    private long start = 0;

    public TicketServer() {
    }

    public void setIp(Map<String, Integer> ip) {
        this.ip = ip;
        initList(ip);
    }

    private void initList(Map<String, Integer> ip) {
        // 清空List
        serverList.clear();
        // 重建一个Map，避免服务器的上下线导致的并发问题
        Map<String, Integer> serverMap = new HashMap<>(ip);
        // 取得Ip地址List
        for (String server : serverMap.keySet()) {
            int weight = serverMap.get(server);
            for (int i = 0; i < weight; i++) {
                serverList.add(server);
            }
        }
    }

    public String getServer() {
        String server;
        lock.lock();
        try {
            if (pos >= serverList.size()) {
                pos = 0;
            }
            server = serverList.get(pos);
            pos++;
        } finally {
            lock.unlock();
        }
        return server;
    }

    public String connect(String path, String data) {
        String server = getServer();
        try {
            return HttpUtil.connect("http://" + server + "/" + path)
                    .setData("data", data)
                    .setMethod("POST")
                    .execute()
                    .getBody();
        } catch (IOException e) {
            if (System.currentTimeMillis() - start >1000) {
                logger.error("{} The server is not available.", server);
                start = System.currentTimeMillis();
            }
        }
        return null;
    }
}
