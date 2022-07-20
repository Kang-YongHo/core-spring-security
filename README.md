[강의 출처](https://www.inflearn.com/course/%EC%BD%94%EC%96%B4-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%8B%9C%ED%81%90%EB%A6%AC%ED%8B%B0/dashboard)
===

## 섹션1. 스프링 시큐리티 기본 API 및 Filter 이해
UsernamePasswordAuthenticationFilter
---
- 필터가 최초에 리퀘스트 요청을 받게 된다.
- 요청 정보의 Url을 받아 AntPathRequestMatcher에서 매칭되는지 확인
  - no: chain.doFiler
  - yes: Authentication(Username + Password)
- 인증된 정보를 AuthenticationManager가 받아 AuthenticationProvider에게 위임을 하게 된다.
  - 실패시: AuthenticationException이 발생해 다시 Filter로 돌아가 응답을 처리하게 됨
  - 성공시: 성공한 Authentication객체를 만들어 AuthenticationManager에게 응답을 준다.
- AuthenticationManager는 전달받은 Authentication(User+Authorities)를 SecurtiyContext에 저장한다.
- SecurityContext는 Sesssion에 인증정보를 저장하고 이후에 이용할 수 있게 해준다
- SuccessHandler 동작

Logout
---
- LogoutFilter가 요청을 받는다
- AntPathRequestMatcher가 로그아웃 URL인지 확인
  - no: chain.doFilter
  - yes: Authentication
- Authentication
- SecurityContextLogoutHandler가 받아 세션무효화, 쿠키삭제, SecurityContextHolder.clearContext()를 실행하게 된다.
- Handler가 성공적으로 종료가 되면 Filter는 SimpleUrlLogoutSuccessHandler를 호출해 설정된 주소로 리다이렉트 한다.

Remember Me
---
- Spring Security에서는 유저정보를 기억할 수 있는 Remember Me API를 제공하고 있다.
- 세션이 만료되고 웹 브라우저가 종료되도 어플리케이션이 사용자를 기억하는 기능이다.
- Remember-Me 쿠키에 대한 http요청을 확인 후 **토큰 기반 인증**을 사용해 유효성 검사하고, 검증되면 사용자가 로그인 된다.

사용자 라이프 사이클
---
* 인증 성공(Remember-Me 쿠키 설정)
* 인증 실패(쿠키가 존재하면 쿠키 무효화)
* 로그아웃(쿠키가 존재하면 쿠키 무효화)


RememberMeAuthenticationFilter
---
- 조건: Authentication객체가 Null일 때 동작한다.
- Spring Security인증을 받으면 Authentication객체는 항상 SecurityContext에 존재하게 된다.
- 그러나 해당 유저의 세션이 만료됐거나 끊겨서 세션안에서 SecurityContext를 찾지 못하는 경우 Null이 될 것이고 그때 RememberMeAuthenticationFilter가 동작한다.
- 두번째 상황은 유저가 최초에 폼 인증을 받을 때 리멤버미를 이용해 쿠키를 발급 받은 경우 다시 서버에 접속하려하면 헤더에 있던 값을 이용해 인증을 시도 할 때이다.
- 요청이 들어오게 되면 RememberMeAuthenticationFilter가 동작
- RememberMeServices 인터페이스의 구현체인 TokenBasedRememberMeServices와 PersistentTokenBasedRememberMeServices가 실제 인증처리를 하게 된다.
  - TokenBasedRememberMeServices
    - 메모리에서 실제로 저장한 토큰과 유저의 토큰과 맞는지 검사한다.
  - PersistentTokenBasedRememberMeServices
    - 유저가 요청한 토큰과 DB에 저장된 토큰이 맞는지 검사한다.
- TokenBasedRememberMeServices 로직
  - Token Cookie추출
  - remember-me Token이 존재하는가
    - no: chain.doFilter
    - yes: Decode Token
      - no: Exception
      - yes: Token이 서로 일치하는가?
        - no: Exception
        - yes: User계정이 존재하는가?
          - no: Exception
          - yes: 새로운 Authentication 생성
  - AuthenticationManager에 전달해 인증 진행

AnonymousAuthenticationFilter
---
- 이 필터는 Authentication정보가 없는 유저에 대해 null처리를 하는게 아닌 익명사용자로 처리한다.
- 즉, 익명사용자와 인증사용자를 구분해서 처리하기 위해 사용
- 요청 시 필터가 인증객체가 존재하는지 확인한다.
- 인증객체가 존재한다면 다음필터로 이동한다.
- 만약 존재하지 않는다면, ROLE_ANONYMOUS권한 정보를 가진 AnonymousAuthenticationToken을 생성해 SecurityContext에 담게 된다.
- 화면에서 인증 여부를 구현할 때 isAnonymous()와 isAuthenticated()로 구분해서 사용한다
- 익명사용자의 경우 인증객체는 세션에 저장하지 않는다.

세션 제어
---
동시세션제어
- 최대 세션 허용 개수 초과를 하지 않게 제어한다.
- 두 가지 방법이 있다. 이전 사용자 세션 만료와 현재 사용자 인증 실패
- http.sessionManagement() 함수를 통해 세션관리 기능을 동작 시킨다.

세션 고정 보호
  - 사용자 인증마다 새로운 세션,쿠키를 발급함으로써 세션탈취공격을 방지하는 방식
    - 기본값(changeSessionId)
    - none
    - migrateSession(이전 세션에 덮어씌움)
    - newSession(세션이 새로 발급되지만 이전 세션에 발급된 속성값을 전부 새로 설정해줘야함)

세션 정책
- SessionCreationPolicy를 이용한 정책 설정
  - Always: 스프링 시큐리티가 항상 세션 생성
  - If_Required: 필요 시 생성(기본값)
  - Never: 생성하지않지만 이미 존재하면 사용
  - Stateless: 생성하지 않고 존재해도 사용하지 않음

SessionManagementFilter, ConcurrentSessionFilter
---

SessionManagementFilter
- 역할
  - 세션관리: 인증 시 사용자의 세션정보를 등록, 조회, 삭제 등의 세션 이력을 관리
  - 동시적 세션 제어: 동일 계정으로 접속이 허용되는 최대 세션수를 제한
  - 세션 고정 보호: 인증 할 때마다 세션 쿠키를 새로 발급하여 공격자의 쿠키 조작을 방지
  - 세션 생성 정책: Always, If_Required, Never, Stateless

ConcurrentSessionFilter
- 역할
  - 매 요청마다 현재 사용자의 세션 만료 여부 체크
  - 세션이 만료되었을 경우 즉시 만료 처리
  - session.isExpired() == true
    - 로그아웃 처리
    - 즉시 오류페이지 응답

SessionManagementFilter, ConcurrentSessionFilter 인증 흐름
1. ConcurrentSessionControlAuthenticationStrategy
2. ChangeSessionIdAuthenticationStrategy
3. RegisterAuthenticationStrategy
- **여기서 1.5에서 2.5 정도에 SessionManagementFilter가 위치해있다.**
4. 3에서 세션(session 1)이 등록 된다.
5. 이때 다른 위치에서 로그인을 또 하게되면 maxSessions를 체크해 초과한다면 기존 세션을 만료 시키고 새로운 세션을 등록한다(session 2)
6. 이후에 sessions 1이 서버에 요청을 보내면 세션만료가 됐다는 인포를 받게된다.

권한 설정 및 표현식
---
- 선언적 방식, 동적 방식(DB 연동 프로그래밍)이 있다
- URL방식: http.antMatchers("/users/**").hasRole("USER")
  - 설정 시 구체적인 경로가 먼저 오고 그것보다 큰 범위의 경로가 뒤에 와야한다
  - 표현식: SecurityConfig에 작성.
- method: @PreAuthorize("hasRole('USER')") 

ExceptionTranslationFilter, RequestCacheAwareFilter
---
- ExceptionTranslationFilter
  - AuthenticationException(인증)과 AccessDeniedException(인가)에 대한 예외 처리를 담당한다.
  - ExceptionTranslationFilter try/catch로 감싸서 FilterSecurityInterceptor를 호출해 예외 처리
- AuthenticationException
  - 인증예외 처리
    - AuthenticationEntryPoint 호출
      - 로그인 페이지 이동, 401오류 코드 전달 등
    - 인증 예외가 발생가지 전의 요청 정보를 저장
      - RequestCache - 사용자의 이전 요청 정보를 세션에 저장하고 이를 꺼내 오는 캐시 메카니즘
        - SavedRequest - 사용자가 요청했던 request 파라미터 값들, 그 당시의 헤더값들 등이 저장
- AccessDeniedException
  - 인가 예외 처리
    - AccessDeniedHandler에서 예외 처리하도록 제공
- 인증/인가 흐름
  - 인증정보가 없는 유저가 페이지에 접근한다고 가정
    - FilterSecurityInterceptor에 의해 인증 예외인지 인가 예외인지 구분해 ExceptionTranslationFilter로 넘기게 된다.
      - *좀 더 정확히는 최초에는 인가예외를 발생시키게 되지만 AccessDeniedHandler로 넘기지 않고 AuthenticationException으로 넘기게 된다.*
      1. AuthenticationException에 넘어갔다가 인증실패 처리 후 AuthenticationEntryPoint로 넘겨 로그인 페이지로 리다이렉트 시킨다. 
      2. 실패처리를 하기 전에 요청을 보냈던 유저의 요청정보를 DefaultSavedRequest객체에 저장을 하고 이 객체를 세션에 저장하게 된다. 이 과정은 HttpSessionRequestCache에 의해 동작한다.
  - 인가정보가 없는 유저가 페이지에 접근한다고 가정
    - FilterSecurityInterceptor에 의해 인증 예외인지 인가 예외인지 구분해 ExceptionTranslationFilter로 넘기게 된다.
      - AccessDeniedException을 발생시키고 AccessDeniedHandler를 호출해 후속작업 처리한다.(자원에 접근못한다는 메세지라던가 등등)
- RequestCacheAwareFilter
  - 현재 세션에 캐싱돼있던 리퀘스트 객체를 가져와 해당 정보에 맞게 리다이렉트 해준다.
- 실습코드는 SecurityConfig에 exceptionHandling()위치에 있다.

CSRF, CsrfFilter
---
- CSRF(사이트 간 요청 위조)
  - 공격자가 특정 사용자에게 심어놓은 코드를 통해 특정 사이트에 주입함으로써 공격자가 의도한 대로 요청에 대한 응답을 받는 것
- CsrfFilter
  - 모든 요청에 랜덤하게 생성된 토큰을 HTTP 파라미터로 요구
  - 요청 시 전달되는 토큰 값과 서버에 저장된 실제 값과 비교한 후 만약 일치하지 않으면 요청은 실패한다.
- Spring Security에서 CSRF는 활성화가 Default이다
- 

















