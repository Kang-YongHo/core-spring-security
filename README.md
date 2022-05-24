강의 출처: https://www.inflearn.com/course/%EC%BD%94%EC%96%B4-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%8B%9C%ED%81%90%EB%A6%AC%ED%8B%B0/dashboard
===

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