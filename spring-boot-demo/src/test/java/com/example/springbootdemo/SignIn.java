package com.example.springbootdemo;

import java.util.ArrayList;
import java.util.List;

public class SignIn {
    private List<People> list = new ArrayList<>();

    public SignIn(People people) {
        assert people.getPosition()== People.Post.LEADER;
    }

    /**
     * 签到
     */
    public void add(People people) {
        list.add(people);
    }

    /**
     * 默认add顺序
     */
    public List<People> getAll() {
        return list;
    }

    /**
     * 签到人数
     */
    public int size() {
        return list.size();
    }

    /**
     * 以ID排序
     */
    public List<People> getAllSort() {
        ArrayList<People> arrayList = new ArrayList<>(list);
        arrayList.sort(null);
        return arrayList;
    }

    /**
     * 倒数第几个
     */
    public People get(int i) {
        if (i>list.size()){
            return null;
        }
        return list.get(list.size()-i);
    }


}
