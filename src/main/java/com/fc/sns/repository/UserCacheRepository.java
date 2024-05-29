package com.fc.sns.repository;

import com.fc.sns.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserCacheRepository {
    private final RedisTemplate<String, User> userRedisTemplate;
    private final static Duration USER_CACHE_TTL = Duration.ofDays(3); // TTL 즉 Expired 유효시간을 걸어준다. (Redis 공간을 효율적으로 사용하기 위해)

    public void setUser(User user) {
        String key = getKey(user.getUsername());
        log.info("Set User to Redis {}, {}", key, user);
//        userRedisTemplate.opsForValue().set(key, user, USER_CACHE_TTL); // TTL이 있기 때문에 새로 덮어 씌워도 됨..
        Boolean aBoolean = userRedisTemplate.opsForValue().setIfAbsent(key, user, USER_CACHE_TTL); 
        // setIfAbsent: key 존재 여부 확인 - 이미 존재할경우 다른 스레드가 lock획득 했다는 의미 (true: 최초 lock 획득- 최초 저장 / false: 다른 스레드에서 이미 저장됨 - 락획득 실패)
        log.info("SetIfAbsent - Lock T/F : {}", aBoolean);
    }

    public Optional<User> getUser(String userName) {
        String key = getKey(userName);
        User user = userRedisTemplate.opsForValue().get(getKey(userName));
        log.info("Get Data from Redis {}, {}", key, user);
        return Optional.ofNullable(user);
    }

    /**
     * 회원 조회시 가장 많이 하는 key는 userName이다. <br/>
     * User를 캐싱하기로 했을 때 가장 많이 조회되는 부분이 JwtTokenFilter 이다. <br/>
     * 매 API요청시마다 loadByUserName을 통해 DB 조회가 이루어진다. <br/>
     *
     * Redis는 하나의 클러스터로 만들어 놓고 Service에서 사용되는 모든 캐싱을 넣게 된다. <br/>
     * User뿐만 아니라 Post, Comment, Like, Alarm 등 여러가지를 캐싱하게 될 수 있다 <br/>
     * 이와 같은 상황에서 Key를 userName 으로만 지정하게 되면 이게 어떤 데이터의 키 값인지 알아보기 어려울 수 있다. <br/>
     * 예를들어 comment라고 했을 때도 userName이라고 지정하게 되면 어떤 데이터의 key인지 알아보기 어렵기 때문이다. <br/>
     * 따라서 key 저장시 Prefix를 지정하는것이 관례이다.
     * @param userName
     * @return
     */
    private String getKey(String userName) {
        return "USER:" + userName;
    }
}
