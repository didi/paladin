Paladin是由小桔车服算法团队出品的智能化触达引擎。算法团队基于租车、养车、能源等业务近100个营销及风控场景近4年时间的沉淀，抽象出了一套基于DR-DCG（Dynamic Routing Directed Cycle Graph）的策略引擎，并结合可视化界面降低配置成本，支持复杂算法策略快速接入。在工程实现层面，Paladin借助响应式编程模型框架，结合插件化热部署、动态脚本等技术，极大程度降低了策略开发部署成本，同时对核心算法保留可扩展接口，最大化平衡策略上线的灵活性和便利性。

#### 核心能力：

- 实现一套基于DR-DCG的策略框架，支持图结构可视化和配置化，其中Vertex是触达行为（Action），Edge是Condition或Weight，当Action执行后会生成对应的Event；我们不仅能够实现基于规则的机械式触达（边是Condition），也可以实现基于算法的自适应触达（边是Weight），每条边的权重在触达链反复迭代中能够通过强化学习（比如Multi-Armed Bandit算法）持续优化，当规则出现冲突或缺失时，系统会自动切换到基于算法的触达方式选择（结合Condition和Weight）下一个Action。
- 在DR-DCG内部，基于事件驱动编程模型（Akka框架）实现最优Action选择（比如贪心算法、DP算法等），从而基于触达反馈数据自适应生成最优触达方案。

- 支持Action插件化热部署，支持Condition动态脚本（Groovy、Aviator等）可扩展能力。
- 支持Conditon、Action、Event不同级别的State持久化，实现基于Memory、Redis、MySQL等不同引擎的持久化存储能力。

- 支持分桶测试能力，比较不同策略执行效果。
- 对触达链每一步动作记录详尽的日志，提供API及界面方便用户定位问题。

#### 核心思路：

![](https://s3-ep-inter.didistatic.com/didoc-upload-image-prod/1604047747816/%E8%87%AA%E5%8A%A8%E5%8C%96%E7%AD%96%E7%95%A5%E5%BC%95%E6%93%8E.png)