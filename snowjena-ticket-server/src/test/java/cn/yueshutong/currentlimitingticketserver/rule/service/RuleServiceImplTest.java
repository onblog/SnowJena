package cn.yueshutong.currentlimitingticketserver.rule.service;

import cn.yueshutong.monitor.entity.MonitorBean;
import org.junit.Test;

public class RuleServiceImplTest {

    @Test
    public void check() {
        double s = 0.00000031 / 4;
        System.out.println(s == 0.00000031 / 4 ? s : false);
    }

    @Test
    public void entity(){
        MonitorBean monitorBean = new MonitorBean();
        monitorBean.setName("A");
        to(monitorBean);
        System.out.println(monitorBean.getName());
    }

    private void to(MonitorBean monitorBean) {
        monitorBean = new MonitorBean();
        monitorBean.setName("B");;
    }

    @Test
    public void test1(){
        test2("","","");
    }
    public void test2(String... name){
        StringBuilder builder = new StringBuilder();
        for (String value : name) {
            builder.append(value);
        }
        System.out.println(builder.toString());
    }
}