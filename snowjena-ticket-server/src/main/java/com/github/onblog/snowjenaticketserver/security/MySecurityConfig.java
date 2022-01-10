package com.github.onblog.snowjenaticketserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class MySecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private TicketProperties user;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin(); //解决不加载iframe
        http.csrf().ignoringAntMatchers("/monitor", "/heart","/token","/rule"); //解决CSRF导致的POST响应403

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST,"/monitor", "/heart", "/token").permitAll() //允许不认证访问
                .anyRequest().authenticated() //其他所有资源都需要认证，登陆后访问
                .and()
                .formLogin()//开启自动配置的授权功能
                .and()
                .logout()//开启自动注销
                .and()
                .rememberMe()
        ;
    }

    /*定义认证规则*/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //保存用户信息到内存中
        auth.inMemoryAuthentication()
                .passwordEncoder(new MyPasswordEncoder()) //2.0后必须实现该接口
                .withUser(user.getUsername()).password(user.getPassword()).roles("ADMIN");
    }

    /*忽略静态资源*/
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/resources/static/**");
    }
}
