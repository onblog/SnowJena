package cn.yueshutong.currentlimitingticketserver.rule.service;

import org.junit.Test;

import static org.junit.Assert.*;

public class RuleServiceImplTest {

    @Test
    public void check() {
        double s = 0.00000031 / 4;
        System.out.println(s == 0.00000031 / 4 ? s : false);
    }
}