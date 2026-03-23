package com.yhg.olivemarket.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 *
 * 장바구니 데이터를 Redis에 저장하기 위한 설정이다.
 * - 커넥션 팩토리: Lettuce (비동기, 성능 우수)
 * - 직렬화 방식: Key/Value 모두 String (사람이 읽기 쉬운 형태로 저장)
 *
 * Redis에 저장되는 장바구니 구조:
 *   Key   → "cart:{memberId}"
 *   Field → "{productId}"
 *   Value → "{quantity}"  (Redis Hash 구조 사용)
 */
@Configuration
public class RedisConfig {

    /**
     * application.yml의 spring.data.redis.host 값 주입
     */
    @Value("${spring.data.redis.host}")
    private String host;

    /**
     * application.yml의 spring.data.redis.port 값 주입
     */
    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 커넥션 팩토리 Bean 등록
     *
     * Lettuce: Netty 기반의 비동기 Redis 클라이언트
     * - 스레드 안전 (커넥션 공유 가능)
     * - 논블로킹 I/O 지원
     * - Spring Data Redis 기본 클라이언트
     *
     * @return LettuceConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * RedisTemplate Bean 등록
     *
     * RedisTemplate<String, String>:
     * - Key: 문자열 (예: "cart:1")
     * - Value: 문자열 (예: 수량 "3")
     *
     * StringRedisSerializer: 객체를 UTF-8 문자열로 직렬화
     * → 기본 JdkSerializationRedisSerializer 대신 사용하면
     *   Redis CLI에서도 데이터를 눈으로 확인 가능
     *
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key 직렬화: "cart:1" 같은 문자열 그대로 저장
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Value 직렬화: "3" 같은 문자열 그대로 저장
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        // Hash Key 직렬화 (Hash 자료구조 사용 시)
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value 직렬화
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
