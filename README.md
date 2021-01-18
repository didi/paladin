# PALADIN

Paladin is an intelligent reach engine.

# Overview

Paladin is an intelligent reach engine produced by Didi xiaojuchefu algorithm team. It is based on DCG (Dynamic Routing Directed Cycle Graph) and combined with the visual interface to reduce configuration cost and support complex algorithms.

Paladin is implemented in the responsive programming model framework and combined with dynamic scripting, plugin-deployment to greatly reduce the cost of development and deployment. It also keeps the extensible interface for the algorithm to maximize the convenience and flexibility of the deployment.

# Feature

* Implement DR-DCG-based strategy framework, support visualization, and configuration of the graph structure.

* Support optimal Action selection based on event-driven programming model(Akka framework)

* Support Action plug-in hot deployment, support Condition dynamic script (Groovy, Aviator, etc.) expandability.

* Supports state persistence at different levels of Condition, Action, Event, and realizes persistent storage capabilities based on different engines such as Memory, Redis, and MySQL.

* Support buckets testing to compare different strategies.

* Support detailed log, API, interface of each step of reach chain to locate problems.

# Architecture

## Environment

•   **Language** : Java 8+、SCALA、JAVASCRIPT

•	**IDE(Java)** : IDEA/Eclipse installs the Lombok plug-in、VSCode

•	**Dependency management** : Maven、npm

•	**Database** : MySQL5.7+

## Back end

•	**Base framework** : Spring Boot 2.2.0.RELEASE、Akka

•	**ORM framework** : Mybatis-Plus 3.3.0

•	**Log** : logback

## Front end

•	**Language** : React

•	**Component** : antd

•	**Framework** : dva

•	**Scaffold** : create-react-app

•	**Bundler Tool** : webpack

## Contributing

Any contribution is welcome. All issues and pull requests are highly appreciated! For more details, please refer to [the contribution guide](CONTRIBUTING.md).

## Community

**dingtalk : **

![dingtalk](Paladin_DINGDING.JPG =100*100)

## License

<img alt="Apache-2.0 license" src="https://www.apache.org/img/ASF20thAnniversary.jpg" width="128">

ALITA is licensed under the terms of the Apache license. See [LICENSE ](LICENSE)for more information.