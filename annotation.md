public @interface CustomCacheable
이건 Java 언어 차원에서:

Annotation Type Declaration

JVM 내부적으로는:

java
코드 복사
public interface CustomCacheable extends java.lang.annotation.Annotation
으로 컴파일됩니다.

즉 .class 파일 안에는:

java
코드 복사
interface CustomCacheable {
CacheStrategy cacheStrategy();
String cacheName();
String key();
long ttlSeconds();
}
처럼 메서드만 있는 인터페이스로 저장됨.

하지만 차이점:

일반 interface	annotation
직접 구현 가능	JVM이 구현
new 가능	new 불가
로직 없음	값만 저장

2️⃣ 어노테이션의 “값”은 어디에 저장되나?
java
코드 복사
@CustomCacheable(
cacheStrategy = REDIS,
cacheName = "user",
key = "#userId",
ttlSeconds = 300
)
이건 .class 파일에 이렇게 들어감:

java
코드 복사
RuntimeVisibleAnnotations:
- CustomCacheable {
  cacheStrategy = CacheStrategy.REDIS
  cacheName = "user"
  key = "#userId"
  ttlSeconds = 300
  }
  즉

JVM은 메서드에 붙은 어노테이션을
메타데이터 테이블로 저장함

3️⃣ RetentionPolicy.RUNTIME 의 의미
java
코드 복사
@Retention(RetentionPolicy.RUNTIME)
이 한 줄은 JVM에게 이렇게 말하는 겁니다:

“ClassLoader가 이 클래스를 로딩할 때
이 어노테이션을 메모리에 유지해라”

그래서 가능해지는 것:

java
코드 복사
Method m = ...
CustomCacheable ann = m.getAnnotation(CustomCacheable.class);
이 호출은:

sql
코드 복사
JVM → Method 구조체 → RuntimeVisibleAnnotations → CustomCacheable proxy 반환
4️⃣ JVM이 만들어주는 “가짜 구현체”
이 코드:

java
코드 복사
CustomCacheable ann = m.getAnnotation(CustomCacheable.class);
여기서 ann은?

JVM이 생성한 동적 프록시 객체

내부적으로:

java
코드 복사
class CustomCacheable$$Proxy implements CustomCacheable {
return REDIS;
return "user";
return "#userId";
return 300;
}
처럼 동작하는 메모리 기반 객체

이건 실제 클래스 파일이 아님 → JVM 내부 구조체

5️⃣ ElementType.METHOD 의 의미
java
코드 복사
@Target(ElementType.METHOD)
컴파일러가 체크:

java
코드 복사
@CustomCacheable
private String name;
→ 컴파일 에러

왜?

Java Compiler가 .class 생성 전에
Target 규칙을 검증하기 때문

JVM에는 영향 없음
→ 순수 컴파일러 규칙

6️⃣ Spring이 이 어노테이션을 발견하는 과정
Spring AOP는 다음을 실행:

pgsql
코드 복사
Method m
↓
m.getAnnotation(CustomCacheable.class)
↓
null 아니면 → 캐시 로직 적용
이 호출은 JVM이

javascript
코드 복사
Method 구조체 → RuntimeVisibleAnnotations → CustomCacheable Proxy 반환
하는 것

7️⃣ Spring이 이 어노테이션을 실제 코드로 바꾸는 구조
Spring AOP에서:

text
코드 복사
Bean 생성
↓
모든 Method 스캔
↓
@CustomCacheable 붙은 메서드 발견
↓
Proxy 생성
Proxy 코드:

java
코드 복사
Object invoke(Method m, Object[] args) {
if (m has @CustomCacheable) {
CacheKey key = parse("#userId");
Object cached = redis.get(key);
if (cached != null) return cached;

      Object result = target.invoke(m, args);
      redis.put(key, result, ttl);
      return result;
}
return target.invoke(m, args);
}
8️⃣ String key() 가 왜 그냥 문자열이 아닌가?
java
코드 복사
key = "#userId"
이건:

Spring Expression Language (SpEL)

즉 어노테이션 값은:

코드 복사
코드가 아니라 DSL
Spring은:

arduino
코드 복사
String → SpEL 파서 → AST → 실행
으로 변환

9️⃣ 이 어노테이션이 진짜 하는 일
이 코드는 사실상:

“이 메서드를 캐시 프록시로 감싸라” 라는 선언

즉:

ini
코드 복사
CustomCacheable = 캐시 프록시 생성 트리거
🔥 결론 (엔진 레벨 요약)
Java Annotation은

class 파일에 저장되는 메타데이터

JVM이 동적 Proxy로 노출

Spring이 Reflection + AOP로 실행 로직으로 변환





어노테이션은 코드가 아니라 데이터입니다.

자바 컴파일러는 다음처럼 클래스 파일에 저장만 합니다.

@Transactional
public void pay() {}


→ .class 파일에는 이렇게 기록됨

Method pay:
RuntimeVisibleAnnotations:
- Transactional


즉

어노테이션은 JVM이 자동 실행하지 않는다
→ Spring 같은 프레임워크가 직접 읽어서 처리

2️⃣ Retention 정책이 운명을 결정한다
@Retention(RetentionPolicy.RUNTIME)


이게 없으면 Spring은 못 봅니다.

Retention	어디에 남는가
SOURCE	컴파일 시 제거
CLASS	class 파일에만
RUNTIME	JVM 로딩 후 Reflection으로 조회 가능

Spring이 사용하는 모든 어노테이션은:

@Retention(RUNTIME)


왜?

→ Reflection + Bytecode 분석으로 읽어야 하기 때문

3️⃣ Spring은 언제 어노테이션을 읽는가?

Spring 부트 시:

SpringApplication.run()
↓
BeanDefinition 읽기
↓
클래스 스캔
↓
Reflection으로 어노테이션 분석


즉

어노테이션은 서버 시작 시 한 번 해석됨

4️⃣ Spring의 핵심 구조: BeanDefinition

Spring은 클래스를 바로 Bean으로 만들지 않습니다.

먼저:

class → BeanDefinition → Bean → Proxy → 실제 객체

BeanDefinition에 저장되는 것
클래스명
스코프
의존성
어노테이션 정보 (@Transactional, @Autowired 등)


어노테이션은
➡ BeanDefinition의 “설계도”에 기록됨

5️⃣ @Component, @Service는 어떻게 Bean이 되나?

Spring은 이걸 실행합니다:

ClassPathScanningCandidateComponentProvider


내부적으로:

1. classpath의 모든 .class 파일 스캔
2. @Component 또는 meta-annotation이 있는지 검사
3. 있으면 BeanDefinition 등록

@Service는 왜 인식되나?
@Service
@Component


즉

Spring은 @Component가 붙은 것만 찾는다
@Service, @Controller는 그냥 별칭

6️⃣ @Autowired는 언제 동작하는가?

Bean 생성 과정:

1. 생성자 호출
2. 필드 생성
3. @Autowired 분석
4. 의존성 주입
5. 초기화 (@PostConstruct)


Spring 내부:

AutowiredAnnotationBeanPostProcessor


이 클래스가 하는 일:

모든 Bean을 Reflection으로 검사
→ @Autowired 필드 찾기
→ ApplicationContext에서 Bean 찾기
→ 강제 set


즉:

@Autowired는 JVM이 아니라
BeanPostProcessor가 Reflection으로 직접 필드에 값 넣음

7️⃣ @Transactional의 진짜 정체

이게 핵심입니다.

@Transactional
public void pay() {}


이건 실제로 이 메서드를 감싸는 Proxy를 만든다

Spring은 이렇게 동작
Bean 생성
↓
@Transactional 발견
↓
Proxy 객체 생성
↓
원래 Bean 대신 Proxy를 Bean으로 등록


Proxy 구조:

class PaymentService$$Proxy {
PaymentService target;

pay() {
transaction.begin();
try {
target.pay();
transaction.commit();
} catch(e) {
transaction.rollback();
}
}
}


즉

어노테이션은 “프록시를 만들라는 설계도”

8️⃣ Spring AOP의 핵심 엔진

Spring이 사용하는 핵심 인터페이스:

BeanPostProcessor


@Transactional은:

InfrastructureAdvisorAutoProxyCreator


이 Bean이

모든 Bean 검사
→ @Transactional 있으면
→ ProxyFactory로 프록시 생성

9️⃣ 왜 private 메서드에 @Transactional이 안 먹히나?

Proxy 구조 때문

proxy.pay() → target.pay()


private 메서드는:

proxy → 접근 불가


그래서

Proxy를 통과하지 않는 호출에는 어노테이션이 적용 안 됨

🔥 10️⃣ 한 줄 요약

Spring Annotation은
JVM 기능이 아니라
“Spring이 Reflection + Proxy + BeanPostProcessor로 구현한 런타임 DSL”