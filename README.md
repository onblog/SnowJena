# SnowJena

<img src="https://img.shields.io/badge/Language-Java8-green.svg" referrerPolicy="no-referrer"/><img src="https://img.shields.io/badge/Maven-3-green.svg" referrerPolicy="no-referrer"/><img src="https://img.shields.io/badge/License-Apache2.0-green.svg" referrerPolicy="no-referrer"/>

## What

基于令牌桶算法实现的分布式无锁限流框架，支持熔断降级，支持动态配置规则，支持可视化监控，开箱即用。

## Document

使用文档：[中文](./CN_README.md)|[English](./EN_README.md)

## Noun

### 限流

当我们设计了一个函数，准备上线，这时候这个函数会消耗一些资源，处理上限是1秒服务3000个QPS，但如果实际情况遇到高于3000的QPS该如何解决呢？本项目提供了当QPS超出某个设定的阈值，系统可以通过直接拒绝或阻塞两种方式来应对，从而起流量控制的作用。

### 降级

接触过Spring Cloud、Service Mesh的同学，都知道熔断降级的概念。服务之间会有相互依赖关系，例如服务A做到了1秒上万个QPS，但这时候服务B并无法满足1秒上万个QPS，那么如何保证服务A在高频调用服务B时，服务B仍能正常工作呢？一种比较常见的情况是，服务A调用服务B时，服务B因无法满足高频调用出现响应时间过长的情况，导致服务A也出现响应过长的情况，进而产生连锁反应影响整个依赖链上的所有应用，这时候就需要熔断和降级的方法。本项目通过设置快速失败策略来对服务自定义进行熔断或降级。

## Preview

<img src="http://ww2.sinaimg.cn/large/006tNc79ly1g4ia12hyx0j31ei0u0dw5.jpg" referrerPolicy="no-referrer"/>

<img src="http://ww1.sinaimg.cn/large/006tNc79ly1g4ia0rnxjmj31ei0u046y.jpg" referrerPolicy="no-referrer"/>

<img src="http://ww1.sinaimg.cn/large/006tNc79ly1g4ia1dvmasj31ei0u0ngj.jpg" referrerPolicy="no-referrer"/>


## About

Blog：<http://www.yueshutong.cn/>

Email：[yster@foxmail.com](mailto:yster@foxmail.com)

Github：<https://github.com/yueshutong/SnowJena>

Gitee：<https://gitee.com/zyzpp/SnowJena>

交流QQ群：781927207

如果帮助到你了，请不吝赞赏！谢谢！

<img src="http://ww3.sinaimg.cn/large/006tNc79ly1g43096t4oaj30tc0tc41y.jpg" width="300px" referrerpolicy="no-referrer">

<div id="wechat-public" style="text-align: center;">
<p style="font-size: 12px;color: #828282;font-family: serif;">
微信关注公众号
</p>
<img src="https://files.cnblogs.com/files/yueshutong/wechatgood.jpg.bmp" width="300px" referrerPolicy="no-referrer" />
</div>