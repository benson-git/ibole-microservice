#笔记
===

	public abstract <S extends NotifyOnServerBuild & BindableService> T addService

This is the first time I have ever seen dual interface in generics actually used in all my java experience.

------
 Since two proto method's can't have the same name (protoc fails)
 
 
------ 
## 分布式追踪
 https://github.com/opentracing/opentracing-java
 https://github.com/grpc-ecosystem/grpc-opentracing
 
------- 
If the RPC completes normally, onCompleted will be called. If there is an error, onError will be called. If an RPC has not yet started, it will try to reconnect until it is ready. If there is a disconnect in the middle of an RPC, it will not be retried.
If you are using a Streaming RPC, your connection will be kept alive. You can send your batch of data, and then just wait for the RPC to complete. As long as you have not yet received onCompleted or onError, the connection will be kept alive.

------

## About the API version discussion

+ Microservices will not required to maintain a version number in its path. Version number of an API is a metadata information, which need to maintain separately. Best option is to use swagger annotations for this.  
+ Maintaining several version of same API in a single product is cumbersome and will be difficult maintain. Hence every time we will expose only one versions of the API in a product.
+ Best way to expose different versions of same API is to have different versions of the product​​​ and use API gateway ​to rout​e to the correct ​product ​instances​ which expose correct API implementation​.​​
​+ When there is an API addition or change​, ​version number has to increment.

## 错误码管理(非数字形式含义明确、按业务区分避免重复等)

-------


超时

在 gRPC 中没有找到传统的超时设置，只看到在 stub 上有 deadline 的设置。但是这个是设置整个 stub 的 deadline，而不是单个请求。

后来通过一个 deadline 的 issue 了解到，其实可以这样来实现针对每次 RPC 请求的超时设置：

for (int i=0; i<100; i++) {
    blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS).doSomething();
}
这里的 .withDeadlineAfter() 会在原有的 stub 基础上新建一个 stub，然后如果我们为每次 RPC 请求都单独创建一个有设置 deadline 的 stub，就可以实现所谓单个 RPC 请求的 timeout 设置。

 implement an interceptor that customizes the CallOptions passed to
the underlying channel to enforce the desired deadline

------

com.google.common.base.Throwables.getStackTraceAsString

--------

https://github.com/grpc/grpc-java/blob/master/documentation/server-reflection-tutorial.md

--------