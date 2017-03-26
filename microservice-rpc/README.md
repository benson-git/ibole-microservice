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
the underlying channel to enforce weithe desired deadline

Context:
  https://github.com/grpc/grpc-java/issues/2829

------

com.google.common.base.Throwables.getStackTraceAsString

--------

https://github.com/grpc/grpc-java/blob/master/documentation/server-reflection-tutorial.md

--------
https://github.com/grpc/proposal/blob/master/A6-client-retries.md

--------
微服务的故障可能是瞬时故障：如慢的网络连接、超时，资源过度使用而暂时不可用；也可能是不容易预见的突发事件的情况下需要更长时间来纠正的故障。
分布式服务的容错是一个不得不考虑的问题，通常的做法有两种：
+ 重试机制：对于预期的短暂故障问题，通过重试模式是可以解决的。
+ 断路器（CircuitBreaker）模式：将受保护的服务封装在一个可以监控故障的断路器对象中，当故障达到一定门限，断路器将跳闸（trip），所有后继调用将不会发往受保护的服务而由断路器对象之间返回错误。对于需要更长时间解决的故障问题，不断重试就没有太大意义了，可以使用断路器模式。

注意事项

在决定如何实现这个模式时，您应考虑以下几点：
+ 异常处理。通过断路器调用操作的应用程序必须能够处理在操作不可用时可能被抛出的异常，该类异常的处理方式都是应用程序特有的。例如，应用程序会暂时降级其功能，调用备选操作尝试相同的任务或获取相同的数据，或者将异常通知给用户让其稍后重试。
+ 异常类型。一个请求可能由于各种原因失败，其中有一些可能表明故障严重类型高于其他故障。例如，一个请求可能由于需要几分钟才能恢复的远程服务崩溃而失败，也可能由于服务暂时超载造成的超时而失败。断路器有可能可以检查发生的异常类型，并根据这些异常本质调整策略。例如，促使切换到开状态（跳闸）的服务超时异常个数要远多于服务完全不可用导致的故障个数。
+ 日志记录。一个断路器应记录所有失败的请求（如果可能的话记录所有请求），以使管理员能够监视它封装下受保护操作的运行状态。
+ 可恢复性。应该配置断路器成与受保护操作最匹配的恢复模式。例如，如果断路器设定出入开状态的时间很长，即使底层操作故障已经解决它还会返回错误。如果开状态到半开状态切换过快，底层操作故障还没解决它就会再次调用受保护操作。
+ 测试失败的操作。在开状态下，断路器可能不用计时器来确定何时切换到半开状态，而是通过周期性地查验远程服务或资源以确定它是否已经再次可用。这个检查可能采用上次失败的操作的形式，也可以使用由远程服务提供的专门用于测试服务健康状况的特殊操作。
+ 手动复位。在一个系统中，如果一个失败的操作的恢复时间差异很大，提供一个手动复位选项以使管理员能够强行关闭断路器（和复位故障计数器）可能是有益的。同样，如果受保护操作暂时不可用，管理员可以强制断路器进入放状态（并重新启动超时定时器）。
+ 并发。同一断路器可以被应用程序的大量并发实例访问。断路器实现不应阻塞并发请求或对每一请求增加额外开销。
+ 资源分化。当断路器使用某类可能有多个底层独立数据提供者的资源时需要特别小心。例如，一个数据存储包含多个分区(shard)，部分分区出现暂时的问题，其他分区可能完全工作正常。如果该场景中的错误响应是合并响应，应用程序在部分故障分区很可能会阻塞整个请求时仍会试图访问某些工作正常的分区。
+ 加速断路。有时失败响应对于断路器实现来说包含足够的信息用于判定应当立即跳闸并保持最小时间量的跳闸状态。例如，从过载共享资源的错误响应可能指示不推荐立即重试，且应用程序应当隔几分钟时间之后重试。
如果一个请求的服务对于特定Web服务器不可用，可以返回HTTP协议定义的“HTTP 503 Service Unavailable”响应。该响应可以包含额外的信息，例如预期延迟持续时间。
+ 重试失败请求。在开状态下，断路器可以不是快速地简单返回失败，而是将每个请求的详细信息记录日志并在远程资源或服务重新可用时安排重试。
+ 对外部服务的不恰当超时。当对外部服务配置的超时很大时，断路器可能无法保护其故障操作，断路器内的线程在指示操作失败之前仍将阻塞到外部服务上，同时很多其他应用实例仍会视图通过断路器调用服务。

断路器模式业界Java实现

GitHub：jrugged：CircuitBreaker类源代码
https://github.com/Comcast/jrugged
https://github.com/Comcast/jrugged/blob/master/jrugged-core/src/main/java/org/fishwife/jrugged/CircuitBreaker.java

GitHub：Netflix/hystrix
https://github.com/Netflix/hystrix
