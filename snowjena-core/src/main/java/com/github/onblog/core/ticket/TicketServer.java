package com.github.onblog.core.ticket;

import com.github.onblog.commoon.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 发票服务器,负载均衡
 * 自动故障服务检测与切换
 */
public class TicketServer {
    private Logger logger = LoggerFactory.getLogger(TicketServer.class);
    private List<String> serverList = new CopyOnWriteArrayList<>(); //读多写少
    private List<String> backupsList = new CopyOnWriteArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    private int pos = 0;
    private long start = 0;


    public void setServer(Map<String, Integer> ip) {
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

    private String getServer() {
        String server;
        lock.lock();
        try {
            if (serverList.size()==0){
                serverList.addAll(backupsList);
                backupsList.clear();
            }
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
            if (System.currentTimeMillis() - start >3000) {
                logger.error("{} The server is not available.", server);
                start = System.currentTimeMillis();
            }
            serverList.remove(server);
            backupsList.add(server);
        }
        return null;
    }
}
