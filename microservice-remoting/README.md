1、下载Lombok.jar http://projectlombok.googlecode.com/files/lombok.jar
 
2、运行Lombok.jar: java -jar  d:\work\Java\libs\lombok.jar 
        数秒后将弹出一框，以确认eclipse的安装路径
        
3、确认完eclipse的安装路径后，点击install/update按钮，即可安装完成

4、安装完成之后，请确认eclipse安装路径下是否多了一个lombok.jar包，并且其
     配置文件eclipse.ini中是否 添加了如下内容:
           -javaagent:lombok.jar
           -Xbootclasspath/a:lombok.jar
     如果上面的答案均为true，那么恭喜你已经安装成功，否则将缺少的部分添加到相应的位置即可
     
5、重启eclipse