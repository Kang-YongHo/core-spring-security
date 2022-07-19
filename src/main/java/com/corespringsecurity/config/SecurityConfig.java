package com.corespringsecurity.config;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //WebSecurityConfigurerAdapter Deprecated됨에 따라 스프링 시큐리티 설정하는 방식이 바뀌었다.
    //https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter

    UserDetailsService userDetailsService;

    @Bean
    public InMemoryUserDetailsManager userDetailsManager(){
        //변경된 방식의 In-Memory Authentication
        UserDetails user = User.withUsername("user")
            .password("{noop}1234")
            .roles("USER")
            .build();
        UserDetails admin = User.withUsername("admin")
            .password("{noop}1234")
            .roles("ADMIN")
            .build();
        UserDetails sys = User.withUsername("sys")
            .password("{noop}1234")
            .roles("SYS")
            .build();
        return new InMemoryUserDetailsManager(user,admin,sys);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /*
        spring security docs
        http
            .authorizeHttpRequests((authorize) -> {
                    authorize
                        .antMatchers("/shop/login").permitAll()
                        .antMatchers("/shop/my-page").hasRole("USER");

                    authorize.mvcMatchers().access((authentication, object) ->
                            Optional.of(hasRole("ADMIN").check(authentication, object))
                                .filter(authorizationDecision -> !authorizationDecision.isGranted())
                                .orElseGet(() -> hasRole("DBA").check(authentication, object)))
                        .anyRequest().authenticated();
                }
            )
        ;*/

        /*표현식 설명
        http
            .antMatcher("표현식설명").authorizeRequests((info)->{
                info.anyRequest()
                    .hasIpAddress("123123")//주어진 IP로부터 요청이 왔다면 허용
                    .hasAuthority("ROLE_USER, ROLE_ADMIN")  //사용자가 주어진 권한 중 어떤 것이라도 있다면 접근을 허용
                    .hasAnyRole("USER,ADMIN")   //사용자가 주어진 역할 중 어떤 것이라도 있다면 접근을 허용
                    .hasAuthority("ROLE_USER")  //사용자가 주어진 권한이 있다면 접근을 허용
                    .hasRole("USER")    //사용자가 주어진 역할이 있다면 접근을 허용
                    .access(hasRole("ADMIN"))   //주어진 SpEL표현식의 평가 결과가 true이면 접근을 허용
                    .rememberMe()   //기억하기를 통해 인증된 사용자의 접근을 허용
                    .anonymous()    //익명사용자의 접근을 허용
                    .denyAll()      //무조건 접근을 허용하지 않음
                    .permitAll()    //무조건 접근을 허용
                    .fullyAuthenticated()   //인증된 사용자의 접근을 허용, rememberMe 인증 제외
                    .authenticated()    //인증된 사용자의 접근을 허용


            });*/

        http
            .authorizeRequests((authorize) -> authorize
                .antMatchers("/user").hasRole("USER")
                .antMatchers("/admin/pay").access("hasRole('ADMIN')")
                .antMatchers("/sys/**").access("hasRole('SYS') or hasRole('ADMIN')")
                .anyRequest().authenticated());

        http
            .formLogin(formLogin ->
                formLogin
//                            .loginPage("/login-page")
                    .defaultSuccessUrl("/") // 성공 시 리다이렉트되는 주소
                    .failureUrl("/login")   // 실패 시 리다이렉트되는 주소
                    .successHandler((request, response, authentication) -> {
                        // 인증 성공 핸들러를 통해 추가적인 행동을 할 수 있다.
                        System.out.println("name :: " + authentication.getName());
                        response.sendRedirect("/");
                    })
                    .failureHandler((request, response, exception) -> {
                        // 인증 실패 핸들러를 통해 추가적인 행동을 할 수 있다.
                        System.out.println("ex :: " + exception.getMessage());
                        response.sendRedirect("/");
                    })
                    .permitAll())
            .httpBasic(withDefaults())
        ;

        http
            .logout()   // 스프링 시큐리티의 로그아웃을 사용하겠다.
            .logoutSuccessUrl("/")  // 로그아웃 성공 시 리다이렉트 되는 주소
            .addLogoutHandler((request, response, authentication) -> {
                //로그아웃 핸들러를 이용해 로그아웃을 어떻게 할 것인지 정책에 따라 개발할 수 있다.
                //기본적으론 세션무효화, 쿠키삭제, SecurityContextHolder.clearContext()를 행한다.
                HttpSession session = request.getSession();
                session.invalidate();
            })
            .logoutSuccessHandler((request, response, authentication) -> response.sendRedirect(
                "/"))    //로그아웃 성공 시 리다이렉트되는 주소
            .deleteCookies("remember-me")  // 어떤 쿠키를 지울것인지 설정 가능하다.
        ;

        http
            .rememberMe()
            .tokenValiditySeconds(3600) //디폴트는 14일
            .alwaysRemember(
                true) //리멈베미 기능을 활성화하지 않아도 항상 실행, 기본적으로는 false로 하는게 좋다. 여기서는 공부용이기 때문에 true
            .userDetailsService(userDetailsService) // 리멤버미 기능을 수행할 때 시스템상에 유저 정보를 조회하는 인터페이스이다. 필수
        ;

        //세션 관리
        http
            .sessionManagement()    // 세션 관리 기능이 동작함
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .sessionFixation()
            .changeSessionId()    //기본값(changeSessionId), none, migrateSession(이전 세션에 덮어씌움), newSession(세션이 새로 발급되지만 이전 세션에 발급된 속성값을 전부 새로 설정해줘야함)
            .invalidSessionUrl("/invalid")  // 세션이 유효하지 않을 때 이동 할 페이지
            .maximumSessions(1)     // 최대 허용 가능 세션 수, -1: 무제한 허용
            .maxSessionsPreventsLogin(true) //동시 로그인 차단, false:기존 세션 만료(default)
            .expiredUrl("/expired")   //세션만료 시 이동할 페이지
        ;

        return http.build();
    }

}
