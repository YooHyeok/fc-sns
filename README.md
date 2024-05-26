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

# *알람타입과 Enum*
현재 설게상으로는 코멘트, 좋아요에 대한 알람 타입이 있다.  
이러한 알람 타입은 추후에 확장이 될 수 있다.  
예를들어 코멘트에도 좋아요 기능이 추가되거나 코멘트에 코멘트를 다는 계층형 댓글 기능이 추가될 수 있다.  
또 인스타그램을 예를 들면 오랜만에 게시글을 올리면 알람이 오기도 한다.  
이와같이 알람은 변화될 수 있는 환경이 매우 많다.  
서비스적으로 생각했을 때 알람을 보내게 되면 특정 회원의 접속을 유도할 수가 있고,  
회원이 해당 서비스를 활성화 해서 이용할 수 가 있다.  
그렇기 때문에 굉장히 많은 종류의 알람이 생길 수 있고, 서비스적으로 굉장히 맣은 변화가 생길 수 있는것이 알람이다.

추가로 알람의 경우 알람을 하나 받는다고 하나의 알람으로 뜨지는 않는다.  
한개의 게시물에 A,B,C 여러 회원이 동시에 좋아요를 했다고 가정한다.  
A가 좋아요를 눌렀습니다. B가 좋아요를 눌렀습니다. C가 좋아요를 눌렀습니다. 라고 하지 않고,  
A외 두명이 좋아요를 눌렀습니다. 라고 알람창에 뜨는 경우가 대부분이다.  
그 이유는 하나씩 다 하다 보면 알람이 너무 많아지기 때문이다.  
예를들어 인기가 매우 많은 연예인이나 셀럽이 게시글을 올리면 좋아요가 1초에 수십,수백개가 찍힐것이다.  
만약 알람창 상단에 10개정도를 출력한다고 할 때 계속해서 알람창 목록이 변경될것이다  
이는 서비스적으로 (회원입장에서) 굉장히 비효율적이고 서버 입장에서 성능상으로 매우 좋지가 않다. (하나씩 저장되므로)  
때문에 알람을 뭉쳐서 보여주는 경우가 많다.  
뭉칠 때는 같은 타입의 알람 끼리 뭉쳐줘야 되기 때문에 따로 컬럼으로 구분해 주는것이 좋다.

이러한 이유로 알람 타입을 컬럼으로 하나 추가한다.

```java
@Entity
@Table(name = "\"alarm\"")
@TypeDef(name="jsonb", typeClass = JsonBinaryType.class)
public class AlarmEntity {

	/*생략*/

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;
	
	/*생략*/
}
```

```java
@RequiredArgsConstructor // final field나 @NonNull에 해당하는 필드만을 매개변수로 받는 생성자를 생성한다. (꼭 필요한 필드만을 초기화 할 수 있도록 생성자를 만들어준다)
@Getter
public enum AlarmType {
    NEW_COMMENT_ON_POST("new comment!"),
    NEW_LIKE_ON_POST("new like!"),
    ;

    private final String alarmText;
}
```

조금 더 깔끔하게 코드를 관리하기 위해 Enum으로 구성한다.  
일반적으로 Enum으로 타입을 구성하게되면, 도메인 성격을 가질 수 있으며,  
각 도메인에 해당하는 데이터 스펙이 변경되더라도, 데이터베이스에 저장된 value를 변경하지 않아도 된다.  
일반적으로 Enum은 상수필드로 구성되어있고, 각 상수필드에 value값을 매핑할 수 있다.  
이러한 특성으로 데이터베이스에 저장되는 값은 value값이 아닌 상수필드명 그 자체로 저장이 가능하기 때문에,  
중간에 spec변경에 의해 각 상수에 대한 value 값이 변경되더라도 마이그레이션에 큰 어려움이 없어진다.



# *JSON 타입 알람 아규먼트와 `@Type` , `@TypeDef`*
현재는 텍스트 자체가 새로운 코맨트, 새로운 좋아요가 발생했습니다. 정도이지만, 사실 실제 SNS 서비스에서 보면  
누가 코멘트를 달았는지 라는 알림이 뜨고, 해당 알림을 눌렀을 경우 해당 포스트로 이동하게 된다.  
ex) __님이 ___게시물에 새로운 코멘트를 달았습니다.  
따라서 해당 알람 자체에 들어가야 하는 정보는 알람을 발생시킨 회원 정보,   
알람 발생의 주체(좋아요 혹은 댓글이 달린 게시글 혹은 댓글 정보)가 필요하다.

지금 당장은 사용하지 않지만, 훗날 알람 기능이 확장 되었을 때 사용할 수 있도록 미리 저장해두기 위해 필드(컬럼)을 구성한다.  
(미리 저장해 둬야 추후 DB 마이그레이션 등의 작업에 있어서 조금 덜 불편하기 때문)

```java
@Data
@AllArgsConstructor
public class AlarmArgs {
    private Integer fromUserId; // 알람 발생시킨 회원
    private Integer targetId; // 알람 발생 주체 (Post, Comment 등)
}
```

JSON 타입으로 저장한다.
JSON타입의 장점은 굉장히 유연하다는 점이다.
지정된 row가 없다.
상황에 따라 필드가 변화될 수 있는데, 변화에 대한 컬럼의 타입을 변경할 필요가 없어진다.
그 이유는 JSON 이기 때문이다.
JSON 형태만 맞는다면 어떤 데이터가 들어가도 문제가 없다

해당 알람 아규먼트 같은 경우엔 위와같이 두개의 필드만 정의를 해놓았지만,
알람 타입에 따라 추가적인 필드가 필요할 수 있고, 변경될 가능성이 높은 타입이다.
그렇기 때문에 json타입으로 해준다면 유연하게 처리할 수 있다.

또한 알람마다 지정된 argument가 있지는 않을것이다.
코멘트를 작성했다고 가정해본다.
__님이 새 코멘트를 작성했습니다. 라는 알람에 대해서는 postId와 commentId가 필요할것이다.
_외 _명이 새 코멘트를 작성했습니다. 라는 알람에 대해서는 복수개의 commentId가 필요할것이다.
위 같은 경우에는 각 알람 타입마다 필드가 하나씩 추가되어야 할것이다.
List<Integer> commentIds
Integer alarmOccurId
이렇게 알람 타입으로 분류되는 여러 경우에 따라 필드가 다수로 구성될 수 있고, 경우에 따라 특정 필드에는 null값이 들어갈 수 도 있다.
만약 이것들을 다 column으로 지정하고, 특정 필드들에 null값으로 저장되어 있다면 이는 데이터 공간이 굉장히 낭비된다.

### build.gradle: JSONB 타입 사용을 위한 외부 라이브러리 추가
```json
dependencies {
	implementation 'implementation 'com.vladmihalcea:hibernate-types-52:2.17.3''
}
```

### @TypeDef와 @Type 적용
```java
@Entity
@Table(name = "\"alarm\"")
@TypeDef(name="jsonb", typeClass = JsonBinaryType.class)
public class AlarmEntity {

	/*생략*/
	
	@Type(type = "jsonb") // postgres에는 json과 jsonb가 있음 - json은 일반저장, jsonb는 압축을하여 저장 및 index를 걸 수 있음.
    @Column(columnDefinition = "json")
    private AlarmArgs alarmArgs;
	
	/*생략*/
}
```

@TypeDef는 사용자 정의 타입에 대한 매핑을 정의한다.  
위와같이 해당 엔티티 클래스레벨에 @TypeDef를 선언하고, typeClass속성에 사용할 클래스인 JsonBinaryType을, name속성에 해당 타입에 대한 이름을 지정한다.  
json 타입 컬럼으로 지정할 필드에 @Type을 선언하고, type속성에 사용할 타입을 지정해준다. type에 지정한 값은 @TypeDef에 정의한 name속성을 참조하게된다.  










 