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
JSON 형태만 맞는다면 어떤 데이터가 들어가도 문제가 없다.  

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
json 타입 컬럼으로 지정할 필드에 @Type을 선언하고, type속성에 사용할 타입을 지정해준다.   
type에 지정한 값은 @TypeDef에 정의한 name속성을 참조하게된다.  

# *Redis와 캐싱*
데이터를 조회할때는 일반적으로 데이터베이스에서 데이터를 가지고 온다.  
이렇게 데이터베이스로부터 데이터를 가지고 오는 것은 부하가 많이 드는 일이다.  
데이터베이스 IO(입/출력)를 사용하여 데이터베이스 로부터 데이터를 쿼링해서 가지고 와야 하는데, 이게 굉장히 비용이 많이 드는 일이다.  

데이터베이스에 부하를 주지 않으면서 데이터를 조회할 수 있는 방법으로는 캐싱이 있다.  

캐싱을 통해 데이터베이스 보다는 조금 더 가볍게 데이터를 저장하고 가져올 수 있다.  

데이터베이스는 Query라는 질의문을 통해 질의를 함으로써 데이터를 가져오지만  
일반적으로 캐싱은  Key-Value형식으로 많이 구성되어 있어, Key에 해당하는 Date를 조회를 요청하게된다.  

캐싱은 여러가지 방식과, 솔루션이 있다.  
그중 가장 많이 사용하는 Redis와 Local Caching에 대해 알아보자.  

|  | Redis                                                                          | Local Caching |
| --- |--------------------------------------------------------------------------------| --- |
| 특징 | In-Memmory 데이터베이스로 Key-Value 형태의 데이터베이스. <br/>다양한 command를 제공하며 Single Thread이다.| 서버 내에 caching 하는 방법.<br/>서버와 라이프사이클을 함께할 수 있다.|
| 장점 | 여러 instance가 하나의 데이터를 공유할 수 있다.<br/>다양한 command를 지원한다.| 네트워크를 타지 않기 때문에 Redis에 비해 비교적 빠르다.|
| 단점 | Local caching에 비해서는 느리다                                                        | 여러 instance로 구성된 서버의 경우 캐시를 공유할 수 없다. |
- 높은 Availability를 위한 몇가지 옵션 존재
    - Sentinel

      In-Memmory : Memmory에 저장이 되기 때문에 휘발성이다.  
      만약 Redis가 죽거나 했을 경우 따로 백업 옵션을 두지 않으면 데이터들이 모두 소멸되지만 캐시로 사용하는 경우 대부분 데이터베이스에 원본 데이터가 있고, 그것을 캐싱해놓는 용도기 때문에 사실상 큰 문제가 없다.  
      하지만 많은 데이터를 캐싱된 채로 Redis가 죽게 된 경우 데이터베이스에서 다시 캐싱해야 하는데 바로 이때 순간적으로 데이터베이스 부하가 굉장히 높아질 것이다.  
      또 Redis가 빠르기 때문에 Redis를 메인으로 쓰는 경우도 있는데, 이때 데이터 유실 발생 가능성이 있고 이 경우 Sentinel이라는 방법을 사용하여 Redis를 여러개로 구성한다.  
      여러개의 Redis Instance를 하나의 Redis처럼 사용할 수 있도록 하는 방법이다.  
      (클러스터링, 마스터-슬레이브 등)

    - Redis Cluster

# *SSE*
SSE에 대한 개념 설명에 앞서, 특정 페이지를 새로고침 할 때 데이터 갱신이 일어나는 것이 아닌 회원이 특정 알림을 받아야 할 상황   
즉, 게시글에 누군가가 좋아요나 댓글을 남겼을 경우, 실시간 리얼타임 까지는 아니더라도 알림을 받는방법에 대해 알아보자.

### 1. Polling

일정 주기를 가지고 서버의 API를 호출하는 방법.  
(티스토리 블로그 Notification 30초 간격으로 조회 API 호출)  
실시간으로 데이터가 업데이트 되지 않는다는 단점이 있다.  
불필요한 요청이 발생하며 따라서 불필요한 서버 부하가 발생한다.  
서비스에 유저가 적을 경우는 상관 없으나 유저가 굉장히 많은 대규모 트래픽 서비스의 경우 굉장히 많은 요청이 발생하여 서버에 큰 부하가 발생하게 된다.  
다만 호환성이 좋다는 장점이 있다.  
             
| Web Browser `--------- Request -------→` Server<br/>Web Browser `← Response + Data Update -` Server<br/>Web Browser `--------- Request -------→` Server<br/>Web Browser `← Response + Data Update -` Server |
|--------------------------------|

[//]: # (Web Browser `--------- Request -------→` Server  )

[//]: # (Web Browser `← Response + Data Update -` Server  )

[//]: # (Web Browser `--------- Request -------→` Server  )

[//]: # (Web Browser `← Response + Data Update -` Server)

### **2. LongPolling**

서버로 요청이 들어올 경우 일정 시간동안 대기하였다가 요청한 데이터가 업데이트 된 경우
서버에서 웹브라우저로 응답을 보낸다.
즉, 일반적으로 구독과 비슷한 개념으로 API에 일정 대기시간을 걸어놓고, 특정 변경사항이 발생할 경우 해당 변경을 감지한 뒤 응답을 주도록 한다.

연결이 된 경우 실시간으로 데이터가 들어올 수 있다는 장점이 있다.
따라서 Polling보단 개선된 형태이지만 데이터 업데이트가 빈번한 경우 Polling과 유사하다.

| Web Browser `--------- Request -------→` Server<br/>Web Browser `　　　　　　　　　　　　　 │` Server<br/>Web Browser `　　　　　　　Data Update ↓` Server<br/>Web Browser `← Response + Data Update -` Server |
|--------------------------------|

[//]: # (Web Browser `--------- Request -------→` Server  )

[//]: # (Web Browser `　　　　　　　　　　　　　 │` Server  )

[//]: # (Web Browser `　　　　　　　Data Update ↓` Server  )

[//]: # (Web Browser `← Response + Data Update -` Server  )

- 예제 코드
    - **서버**: DeferredResult 클래스 사용
        - **DeferredResultMamager 컴포넌트 클래스**

            ```java
            package com.example.longpolling;
            
            import org.springframework.stereotype.Component;
            import org.springframework.web.context.request.async.DeferredResult;
            
            import java.util.concurrent.ConcurrentHashMap;
            import java.util.concurrent.ConcurrentMap;
            
            @Component
            public class DeferredResultManager {
            
                private final ConcurrentMap<String, DeferredResult<String>> userDeferredResults = new ConcurrentHashMap<>();
            
                public void addDeferredResult(String userId, DeferredResult<String> deferredResult) {
                    userDeferredResults.put(userId, deferredResult);
            
                    deferredResult.onCompletion(() -> userDeferredResults.remove(userId));
                    deferredResult.onTimeout(() -> userDeferredResults.remove(userId));
                }
            
                public DeferredResult<String> getDeferredResult(String userId) {
                    return userDeferredResults.get(userId);
                }
            
                public void removeDeferredResult(String userId) {
                    userDeferredResults.remove(userId);
                }
            }
            ```

        - **Controller API**

            ```java
            @RestController
            public class CommentNotificationController {
            
                @Autowired
                private CommentService commentService;
            
                @PostMapping("/subscribe")
                public DeferredResult<String> subscribe(@RequestParam String userId) {
                    return commentService.subscribe(userId);
                }
            
                @PostMapping("/comment")
                public String postComment(@RequestParam String comment, @RequestParam String ownerId, @RequestParam String authorId) {
                    return commentService.postComment(comment, ownerId, authorId);
                }
            }
            ```

        - **Service**

            ```java
            package com.example.longpolling;
            
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.stereotype.Service;
            import org.springframework.web.context.request.async.DeferredResult;
            
            @Service
            public class CommentService {
            
                @Autowired
                private CommentRepository commentRepository;
            
                @Autowired
                private DeferredResultManager deferredResultManager;
            
                public DeferredResult<String> subscribe(String userId) {
                    DeferredResult<String> deferredResult = new DeferredResult<>(60000L, "No updates");
            
                    deferredResultManager.addDeferredResult(userId, deferredResult);
            
                    return deferredResult;
                }
            
                public String postComment(String content, String ownerId, String authorId) {
                    Comment comment = new Comment();
                    comment.setContent(content);
                    comment.setOwnerId(ownerId);
                    comment.setAuthorId(authorId);
                    commentRepository.save(comment);
            
                    DeferredResult<String> deferredResult = deferredResultManager.getDeferredResult(ownerId);
                    if (deferredResult != null) {
                        deferredResult.setResult("새 댓글: " + content);
                    }
            
                    return "Comment posted";
                }
            }
            ```

    - **클라이언트**

        ```html
        <body>
            <h1>Long Polling Client</h1>
            <div id="messages"></div>
        
            <script>
                const userId = 'owner123'; // 댓글 주인의 고유 ID
        
                function subscribe() {
                    fetch('/subscribe?userId=' + userId, {
                        method: 'POST'
                    })
                        .then(response => response.text())
                        .then(message => {
                            if (message !== "No updates") {
                                document.getElementById('messages').innerText += message + "\n";
                            }
                            subscribe(); // 새 메시지를 기다리기 위해 재귀적으로 호출
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            setTimeout(subscribe, 5000); // 에러가 발생하면 5초 후 다시 시도
                        });
                }
        
                document.addEventListener('DOMContentLoaded', (event) => {
                    subscribe();
                });
            </script>
        </body>
        ```


### **3. SSE (Server Sent Events)**

서버에서 웹브라우저로 데이터를 보내줄 수 있다.
웹 브라우저에서 서버쪽으로 특정 이벤트를 구독함을 알려준다.
서버에서는 해당 이벤트가 발생하면 웹 브라우저 쪽으로 이벤트를 보내준다.
다만 서버에서 웹브라우저로만 데이터 전송이 가능하고 그 반대는 불가능하다.
(웹 브라우저에서 데이터를 담아 서버로 전송하는 것은 불가능함 → 단방향)
또한 최대 동시접속 횟수가 제한되어 있다.

| Web Browser `-------- Subscribe ------→` Server<br/>Web Browser `　　　　　　　Data Update ↓` Server<br/>Web Browser `← Response + Data Update -` Server<br/>Web Browser `　　　　　　　Data Update ↓` Server<br/>Web Browser `← Response + Data Update -` Server |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

[//]: # (Web Browser `-------- Subscribe ------→` Server  )

[//]: # (Web Browser `　　　　　　　Data Update ↓` Server  )

[//]: # (Web Browser `← Response + Data Update -` Server  )

[//]: # (Web Browser `　　　　　　　Data Update ↓` Server  )

[//]: # (Web Browser `← Response + Data Update -` Server)


- 예제 코드
    - 서버 (SseEmitter 사용)
        ```java
        @RestController
        public class SseController {
        
            private final static Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
            private final static String ALARM_NAME = "alarm";
            private ThreadLocal<Map<String, SseEmitter>> emitterMapThreadLocal = ThreadLocal.withInitial(ConcurrentHashMap::new);
        
            @GetMapping("/subscribe")
            public SseEmitter subscribe(Authentication authentication) {
                Map<String,SseEmitter> emitterMap = emitterMapThreadLocal.get();
                SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
                User user = (User) authentication.getPrincipal();
                emitterMap.put(user.getId(), sseEmitter); // 수신자 id 기준 emitter 저장
                sseEmitter.onCompletion(() -> emitterMap.remove(user.getId()));
                sseEmitter.onTimeout(() -> emitterMap.remove(user.getId()));
                try {
                    sseEmitter.send(
                            SseEmitter.event()
                                .id("id")
                                .name(ALARM_NAME) // EventSource의 addEventListner로 등록한 이벤트명과 일치하는 이벤트명을 입력해준다.
                                .data("connect completed")
                   );
                } catch (IOException e) {
                    throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
                }
                return sseEmitter;
            }
      
            @PostMapping("/send")
            public void sendNotification(Integer alarmId, Integer userId) {
                Map<String,SseEmitter> emitterMap = emitterMapThreadLocal.get();
                SseEmitter sseEmitter = emitterMap.get(userId); // 수신자 id 기준 emitter 조회
                try {
                    sseEmitter.send(
                            SseEmitter.event()
                            .id(alarmId.toString()) // 반환받을 클라이언트의 EventSource에서 Data를 구분/식별할 수 있는 PK와 같은 값...
                            .name(ALARM_NAME) // EventSource의 addEventListner로 등록한 이벤트명과 일치하는 이벤트명을 입력해준다.
                            .data("new alarm")
                    );
                } catch (IOException e) {
                    emitterMap.remove(user.getId());
                    throw new RuntimeException();
                }
            }
        }
        ```
      - JwtTokenFilter (Header값 추출...)
        ```java
        RequiredArgsConstructor
        public class JwtTokenFilter extends OncePerRequestFilter {
        private final String key;
        private final UserService userService;
        
            private final static List<String> TOKEN_IN_PARAM_URLS = List.of("/api/v1/users/alarm/subscribe");
            
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                
                try {
                    final String token;
                    if (TOKEN_IN_PARAM_URLS.contains(request.getRequestURI())) {
                        log.info("Request with {} check the query param", request.getRequestURL());
                        token = request.getQueryString().split("=")[1].trim();
                    } else {
                        // get Header
                        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
                        if (header == null || !header.startsWith("Bearer ")) {
                            log.error("Error occurs while getting header. header is null or invalid {}", request.getRequestURL());
                            filterChain.doFilter(request, response);
                            return;
                        }
                        token = header.split(" ")[1].trim();
                    }
        
                    /* 토큰 유효 검증 */
                    if (JwtTokenUtils.isExpired(token, key)) {
                        log.error("Key is expired");
                        filterChain.doFilter(request, response);
                        return;
                    }
        
                    /* 토큰으로 부터 username 획득 */
                    String userName = JwtTokenUtils.getUserName(token, key);
        
                    /* username을 통해 회원 엔티티 조회 */
                    log.info("jwtTokenFilter - loadByUsername 호출");
                    User user = userService.loadByUsername(userName);
                    log.info("jwtTokenFilter - loadByUsername 호출 종료");
        
                    /* 회원 정보와 Role 각각 Security의 Pirncipal과 Authorities에 저장 */
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken( //princlipal, credential, authorities
                            user, null, user.getAuthorities()
        //                    user, null, List.of(new SimpleGrantedAuthority(user.getUserRole().toString()))
                    );
        
                    /* Detail 초기화 - request 정보를 함께 넣어준다. */
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        
                } catch (RuntimeException e) {
                    log.error("Error occurs while validating. {}", e.toString());
                    filterChain.doFilter(request, response);
                    return;
                }
                filterChain.doFilter(request, response); // 종료 후 다음 Filter로 request와 response를 넘겨준다.
            }
        }    
            
        ```
    - 클라이언트 (EventSource 사용)
        ```javascript
        const eventSource = new EventSource("http://localhost/subscribe?token=" + localStorage.getItem('token'));
      
        eventSource.addEventListener("open", function (event) {
        console.log("connection opened");
        });

        eventSource.addEventListener("alarm", function (event) {
        console.log(event.data);
        handleGetAlarm();
        });

        eventSource.addEventListener("error", function (event) {
            console.log(event.target.readyState);
            if (event.target.readyState === EventSource.CLOSED) {
            console.log("eventsource closed (" + event.target.readyState + ")");
            }
            eventSource.close();
        });
        ```


### **4. WebSocket**
서버와 웹브라우저 사이에 양방향 통신이 가능한 방법이다.  
채팅과 같은 프로그램에서 많이 사용하는 방법이다.


# *Kafka와 비동기 처리*


https://kafka.apache.org/downloads

위 다운로드 사이트에서 Binary 파일 다운로드 후 띄어쓰기가 없는 경로에 압축 해제( **C:\Kafka )**

- Command Line 명령어를 통한 Zookeeper/Kafka 기동 및 Topic생성, consumer출력
    1. Zookepper 서버 기동  
       **C:\Kafka>** `.\bin\windows\zookeeper-server-start.bat config\zookeeper.properties`
    2. Kafka 서버 기동  
       **C:\Kafka>**`.\bin\windows\kafka-server-start.bat config\server.properties`
    3. Topic 생성  
       **C:\Kafka>**`.\bin\windows\kafka-topics.bat --create --topic alarm --bootstrap-server localhost:9092`
    4. consumer 내역 리얼타임 출력  
       **C:\Kafka>**`.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic alarm --from-beginning`
    5. Topic 목록 확인  
       **C:\Kafka>**`.\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list`


[카프카 공식 문서](https://kafka.apache.org/documentation/)

카프카는 간단하게 메시징 큐라고 말할 수 있다.

메시지를 생성하는 `Producer`, 메시지(이벤트)를 받아서 처리하는 `Consumer`가 있다.  
이때 `Producer`가 생성한 메시지(이벤트)가 어딘가에 저장이 되어야 `Consumer`가 메시지(이벤트)를 읽어올 수 있다.  
`Producer`가 생성한 메시지(이벤트)는 중간에서 다리 같은 역할을 하는 `Broker` 에 파일로 작성(저장) 된다.  
`Consumer` 는 `Broker` 에 저장된 메시지를 읽어온다.  
`Producer` 의 경우 메시지(이벤트)를 생성할 때 Key라는 값을 설정해야 한다.  
Key는 메시지(이벤트)가 어떻게 파티션이 될지를 정하는 값이다.  

![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/b50b643a-a29a-427d-8bb7-8baa73d7de94/6eb9a5a1-8120-40a4-afa9-840aa23b3070/Untitled.png)

### Topic

`Producer` 가 메시지를 생성하고 Broker 저장할 때 Broker 내부에 지정할 수 있는 영역과 같다.  
이는 Topic을 중심으로 메시지가 생성이 되고, Topic으로 부터 메시지를 소비하는 구조라고 생각하면 된다.  
이를 간단하게 말하자면 `Producer` 와 `Consumer` 를 이어주는 Key같은 값이다.  

이처럼 카프카에는 여러가지의 Topic이 생성 될 수가 있다.  
`Producer` 는 Broker 내부의 Topic에 메시지(이벤트)를 전달하고, `Consumer`는 해당 Topic에서 메시지를 읽어오는 방식으로 동작하게 된다.  

이렇게 전달된 메시지(이벤트)는 파티션닝 되어 저장되게 된다.  

예를들어 메시지가 단순하게 파티셔닝이 된다면, 게시글을 업데이트를 했다고 가정한다.
(해당 포스트에 업데이트된 정보가 메시지(이벤트)로 Topic에 전달된다.)

1. 게시글 제목 A를 B로 바꿨다는 이벤트
2. 게시글 제목 B를 C로 바꿨다는 이벤트

결론적으로는 A를 C로 바꾼것, 최종적인 데이터는 C 이긴 하지만, 이것을 `Consumer`에 의해 읽어들여 B로 변경 후 C로 변경했다 라는 순서를 보장하기 위해서 가장 먼저 읽어야 될 것은 B로 변경된 것을 읽어야 하고, 그 다음 C로 변경된 것을 읽어야 한다.  
만약 순서가 바뀌어서 C로 변경된 것을 먼저 읽게 되면 C로 변경 후 B로 변경된 것 처럼 보일 수 있다.  
이 순서가 보장이 되기 위해서는 Key값이 같아야 한다.  
Key값을 바탕으로 파티셔닝이 되는데 `Consumer` 가 읽을 때 파티션을 차례대로 읽는다.  
이는 같은 파티션에서는 나중에 생성된 데이터가 뒤에 오기 때문에 컨슈머에서 더 늦게 처리되는 것이 보장이 되지만, 파티션이 다른 경우에 컨슈머가 파티션을 읽었을 때 더 늦게 저장된 이벤트를 더 빨리 읽게 되면 해당 파티션에 있는 메시지가 먼저 처리될 수 있다.  

Partition0에 B로 변경한 이벤트가 저장되었고, Partition1에 C로 변경한 이벤트가 저장되었다고 가정한다.  

### Consumer Group

`Consumer` 는 Consumer Group이라는 것이 존재하며, 해당 Group에는 여러 `Consumer` 들이 존재한다.  
이는 같은 토픽을 나눠 읽기 위함으로 설계된것이다.

Consumer1이 Partition0에 접근하고 Consumer2가 Partition1에 접근한다.  
이때 특정 상황에 의해 아주 불행하게도 Consumer2가 조금 더 빠르고 Consumer1이 살짝 느려져 버렸다.  
이에 Consumer2가 먼저 partition1의 C로 변경된 메시지를 처리해 버리고 Consumer1이 다음 메시지인 B로 변경된 메시지를 처리하게 되면 결론적으로 게시글을 B로 바꾼 뒤 C로 바꾼것인데
글이 B로 변경된 것 처럼 보일것이다.

이를 방지하기 위해서 Key값을 설정하는 것이다.  
Key값을 기준으로 파티션이 나눠지기 때문에 같은 Key를 가지고 있다면 같은 파티션에 메시지가 적재되게 돼서 하나의 컨슈머가 해당 파티션을 처리되어 절대적으로 메시지의 순서가 바뀌는 현상이 방지된다.

이후 Consumer에서 메시지를 읽은 뒤 완료 신호로 Ack를 날리게 된다.  
(Ack가 전송되야 비로서 메시지가 처리되었다고 표시되는것)  

- 단위별로 10개를 읽으면 날리는 배치성 Ack
- 1개 읽을때 마다 날리는 Ack
- Command에 의해 Ack시점을 결정하는 방법

 