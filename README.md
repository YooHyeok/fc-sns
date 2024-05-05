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

# *Gradle로 Node 빌드, 실행*
Node 베이스의 프론트엔드 코드(React, Vue, Next, Nuxt 등)들을 Gradle로 빌드해야 하는 경우   
gradle.build 파일에 아래와 같이 빌드를 위한 Task를 작성한다.

- ### gradle.build
    ```json
    plugins {
        /* 생략 */
        id "com.github.node-gradle.node" version "3.3.0"
    }
    
    /* 생략 */
    
    node {
        nodeModulesDir = file("$projectDir/front-end")
        version = '18.3.0'
        download = true
    }
    
    task npmBuild(type: NpmTask) {
        args = ['run', "build"]
    }
    
    task copyFrontEnd(type: Copy) {
        from "$projectDir/front-end/static"
        into 'build/resources/main/static/.'
    }
    
    task cleanFrontEnd(type: Delete) {
        delete "$projectDir/front-end/static", "$projectDir/front-end/node_modules"
    }
    
    npmBuild.dependsOn npmInstall
    copyFrontEnd.dependsOn npmBuild
    compileJava.dependsOn copyFrontEnd
    
    clean.dependsOn cleanFrontEnd
    ```

  1. #### *plugins 추가*
     Node.js 플러그인을 사용하여 프로젝트의 Node.js 및 npm 관련 작업을 수행한다.
  2. #### *node*
     프로젝트의 front-end 디렉토리에서 Node.js 모듈을 관리하고, Node.js의 버전을 지정한다.
  3. #### *task npmBuild*
     npm을 사용하여 프런트엔드 빌드를 실행하도록 설정합니다.
  4. #### *task copyFrontEnd*
     프런트엔드의 정적 파일을 빌드된 Java 애플리케이션의 리소스 디렉토리로 복사한다.
  5. #### *task cleanFrontEnd***
     프런트엔드의 정적 파일 및 Node.js 모듈을 삭제한다.
  6. #### *npmBuild.dependsOn npmInstall*
     npmInstall 작업이 완료된 후에 npmBuild 작업이 실행되도록 한다.
  7. #### *copyFrontEnd.dependsOn npmBuild*
     npmBuild 작업이 완료된 후에 copyFrontEnd 작업이 실행되도록 한다.
  8. #### *compileJava.dependsOn copyFrontEnd*
     필요한 의존성을 설정하여 Java 컴파일 작업이 프런트엔드 빌드 및 복사 작업에 의존하도록 한다.  
     (프런트엔드를 빌드한 후에 빌드된 자원들을 Java 애플리케이션의 리소스 디렉토리로 복사하게 됨)
  8. #### *clean.dependsOn cleanFrontEnd*
     `clean` 작업에 `cleanFrontEnd` 작업을 종속시켜 프런트엔드 관련 파일을 삭제한다.  
     `./gradlew clean`