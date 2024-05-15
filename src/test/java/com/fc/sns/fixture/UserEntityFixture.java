package com.fc.sns.fixture;

import com.fc.sns.model.entity.UserEntity;

/**
 * UserServiceTest - 회원가입/로그인 용 Fixture 클래스 <br/>
 * 서로 다른 테스트에서 필요한 동일한 객체를 매번 <br/>
 * 생성해야 하는 상황이 있을 때 중복된 객체를 사전에 <br/>
 * 미리 정의하여 고정해두는 방식으로 구성한다. <br/>
 * JUnit에서는 @BeforeEach 등의 어노테이션을 통해 <br/>
 * 테스트 직전 Fixture 객체의 인스턴스를 생성 및 초기화 <br/>
 * 할 수 있도록 지원하지만, <br/>
 * 현재의 방식은 별도로 테스트에서 사용하는 객체의 생성을 관리할수 있다.
 */
public class UserEntityFixture {
    public static UserEntity get(String userName, String password, Integer userId) {
        UserEntity result = new UserEntity();
        result.setId(userId);
        result.setUserName(userName);
        result.setPassword(password);
        return result;
    }
}
