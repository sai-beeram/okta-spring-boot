/*
 * Copyright 2017 Okta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okta.spring.tests.oauth2.implicit;

import com.okta.spring.boot.oauth.Okta;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class BasicImplicitFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicImplicitFlowApplication.class, args);
    }

    @Configuration
    static class ResourceSecurityConfigurer extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {

            Okta.configureBrowserFriendlyResourceServer401EntryPoint(http);

            http.authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                    .anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();
        }
    }

    @RestController
    @CrossOrigin(origins = "http://localhost:8080")
    public static class MessageOfTheDayController {

        @GetMapping("/api/userProfile")
        @PreAuthorize("hasAuthority('SCOPE_profile')")
        public Map<String, Object> getUserDetails(Authentication authentication) {
            // TODO: validate this is the correct way to get the details
            return (Map<String, Object>) authentication.getDetails();
        }

        @GetMapping("/api/messages")
        @PreAuthorize("hasAuthority('SCOPE_email')")
        public Map<String, Object> messages() {

            Map<String, Object> result = new HashMap<>();
            result.put("messages", Arrays.asList(
                    new Message("I am a robot."),
                    new Message("Hello, world!")
            ));

            return result;
        }

        @GetMapping("/")
        @PreAuthorize("hasAuthority('SCOPE_email')")
        public String getMessageOfTheDay(Principal principal) {
            return "The message of the day is boring: " + principal.getName();
        }

        @GetMapping("/everyone")
        @PreAuthorize("hasAuthority('Everyone')")
        public String everyoneAccess(Principal principal) {
            return "Everyone has Access: " + principal.getName();
        }
    }

    static class Message {
        public Date date = new Date();
        public String text;

        Message(String text) {
            this.text = text;
        }
    }
}