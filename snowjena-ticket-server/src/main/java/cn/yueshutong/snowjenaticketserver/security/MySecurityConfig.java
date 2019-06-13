package cn.yueshutong.snowjenaticketserver.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class MySecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${ticket.server.username}")
    private String username;

    @Value("${ticket.server.password}")
    private String password;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/**").hasRole("ADMIN")
                .and()
                .formLogin()//开启自动配置的授权功能
                .usernameParameter("username")  //自定义用户名name值
                .passwordParameter("password")  //自定义密码name值
                .permitAll() //登录页都可以访问
                .and()
                .logout()//开启自动配置的注销功能
                .logoutSuccessUrl("/")//注销成功后返回到页面并清空Session
                .and()
                .rememberMe()
                .rememberMeParameter("remember")//自定义rememberMe的name值，默认remember-Me
                .tokenValiditySeconds(604800);//记住我的时间/秒
    }

    /*定义认证规则*/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //保存用户信息到内存中
        auth.inMemoryAuthentication()
                .passwordEncoder(new MyPasswordEncoder()) //2.0后必须实现该接口
                .withUser(username).password(password).roles("ADMIN");
    }

    /*忽略静态资源*/
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/resources/static/**");
    }
}
