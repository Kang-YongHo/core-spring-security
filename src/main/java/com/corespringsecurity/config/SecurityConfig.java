package com.corespringsecurity.config;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                .anyRequest().authenticated()
            )
        ;

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
            .logoutSuccessHandler((request, response, authentication) -> response.sendRedirect("/"))    //로그아웃 성공 시 리다이렉트되는 주소
            .deleteCookies("remember-me")  // 어떤 쿠키를 지울것인지 설정 가능하다.
        ;

        http
            .rememberMe()
            .tokenValiditySeconds(3600) //디폴트는 14일
            .alwaysRemember(true) //리멈베미 기능을 활성화하지 않아도 항상 실행, 기본적으로는 false로 하는게 좋다. 여기서는 공부용이기 때문에 true
            .userDetailsService(userDetailsService) // 리멤버미 기능을 수행할 때 시스템상에 유저 정보를 조회하는 인터페이스이다. 필수
        ;

        return http.build();
    }

}
