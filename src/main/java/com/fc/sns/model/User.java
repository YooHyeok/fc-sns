package com.fc.sns.model;

/**
 * 실제 회원 조회후 반환되는 Entity를 대신하여
 * 회원 정보를 제공해줄 수 있도록 도와주는 User DTO용도의 객체
 * 회원정보 응답 spec이 변경 등, 의존성 분리 혹은
 * OSIV 로 인한 커넥션 이슈 성능저하 등을 위해 대게는 DTO로 따로 관리한다.
 */
// TODO: implement
public class User {
    private String userName;
    private String password;
}
