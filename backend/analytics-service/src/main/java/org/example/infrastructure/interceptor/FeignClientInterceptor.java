package org.example.infrastructure.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    ServletRequestAttributes attributes =
      (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (authHeader != null) {
        requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);
      }
    }
  }
}
