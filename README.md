# *DataSourceAutoConfiguration*
일반적으로 SpringDataJpa를 사용하게 될 경우 Database에 대한 URL설정을 따로 하지 않고
애플리케이션의 main thread를 실행하게 될 경우  아래와 같은 오류에 직면하게 된다.

```text
Description:

Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.

Reason: Failed to determine a suitable driver class


Action:

Consider the following:
	If you want an embedded database (H2, HSQL or Derby), please put it on the classpath.
	If you have database settings to be loaded from a particular profile you may need to activate it (no profiles are currently active).
```
이는 SpringDataJpa를 Gradle 디펜던시로 추가할 경우 자동으로 Database 설정을 하여 DB에 연결하려하는데,  
현재 DB URL 관련된 설정을 하지 않았으므로 해당 오류가 콘솔에 출력되는것이다.  
출력된 위 오류는 Embadded Database Path를 설정해주거나 혹은 이러한 설정을 Activate(활성화) 하지 않을 프로파일을  
따로 설정하라는 의미이다.

이에 대한 대안방안으로는 Gradle 디펜던시로 등록한 SpringDataJpa디펜던시를 블록킹 할 수 있다.
다른 방법으로는 애플리케이션 메인 스레드에서 Exclude 할 수 있다.

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SnsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnsApplication.class, args);
    }
}
```
위와같이 Main 클래스의 클래스 레벨에 기본적으로 선언되어있는 `@SpringBootApplication` 어노테이션의 `exclude` 속성을 통해  
`DataSourceAutoConfiguration` 클래스를 지정함으로써 해당 클래스의 기능을 포함하지 않도록 설정한다.
