# 1 概述
本项目是基于 webmagic、springboot和 mongodb 的用于爬取互动百科数据的爬虫。

## 1.1 功能
该项目爬虫适用于爬取互动百科某分类及其子分类的全部词条。

## 1.2 技术选型
jdk 8.0+ （使用了lambda表达式）

[webmagic](https://github.com/code4craft/webmagic)

[springboot](https://github.com/spring-projects/spring-boot)

[mongodb]()

[spring-data-mongodb](https://projects.spring.io/spring-data-mongodb/)

[lombok]() (使用需要添加依赖，并安装IDE插件，[IDEA]()、[Eclipse]() 。不使用仅需为model添加getter/setter方法并删除@Data)

## 1.3 使用

1. 修改application-dev.yml中的mongodb.uri；

2. 修改application-dev.yml中的nameOfRootCategory为需要爬取的根分类；

3. 直接运行BaikeApplication.main，或install后使用 java -jar 命令运行。

# 2 二次开发

## 2.1 词条的发现逻辑

###2.1.1 页面链接分析
以“电影”分类为例。

* “电影”分类的详情页面为 http://fenlei.baike.com/电影 [链接](http://fenlei.baike.com/%E7%94%B5%E5%BD%B1)。

* “电影”分类的全部词条页面为 http://fenlei.baike.com/电影/list/ [链接](http://fenlei.baike.com/%E7%94%B5%E5%BD%B1/list/)

* “电影”分类的子分类在分类页面的“下一级微百科”部分可以获得。

### 2.1.2 链接获取步骤

1. 根据分类名称获取分类的全部词条页面，从该页面获取该分类的全部词条的链接。

2. 根据分类名获取分类详情页，从详情页获取子分类，再到上一步


## 2.2 词条的解析逻辑

页面内容的定位使用的时XPath，html解析使用的是jsoup。

基本上Xpath写对了解析没有什么问题，推荐使用Chrome浏览器ChroPath验证Xpath的正确性。

## 2.3 词条的去重

互动百科的词条的词条名是没有重复的，如果存在如姓名相同的人物介绍词条，都会有相应的解释性注释加到词条名称中。

基于这个特点，完全可以根据词条的名称(title)进行判重。

# 3 趟坑提示

## 3.1 依赖冲突

webmagic-core依赖了log4j、Slf4j和jsoup等项目的特定版本的包，从而会与springboot的或自己添加的依赖包产生冲突。

解决办法就是在webmagic-core的依赖中添加冲突包的exclusions信息。

## 3.2 @Autowire失效

遇到这个问题真是让人百思不得其解，在单元测试用可以使用@Autowire初始化DAO接口，并将数据写入数据库，但是在爬虫中一直是npe错误。

最后的问题出在为Spider设置pipeline时，使用的是 .addPipeline(new BaikeMongodbPipeline()) ，而不是使用@Autowire，这时在BaikeMongodbPipeline中的@Autowire就会失效。

根据网上查到的信息是说，必须在所有直接或间接使用了dao的地方都要使用@Autowired注入，否则就会失效。

## 3.3 webmagic对XPath的支持

以“//p[@id='openCatp']/a”为例。

在xpath标准中应该表示获取id为openCatp的p标签下的全部a标签，

而在webmagic中实际只能获取到第1个a标签。

我目前的做法是干脆直接获取“//p[@id='openCatp']/html()”，然后使用jsoup进行解析。



