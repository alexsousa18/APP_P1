package es.storeapp.web.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class CSPInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        // Vulnerabilidad 17: impedir que la aplicacion se cargue dentro de un iframe
        // de otro sitio (clickjacking), con la cabecera clasica y la directiva CSP moderna.
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Content-Security-Policy",
            "default-src * 'self'; " +
            "img-src * 'self' data:; " +
            "script-src  * 'self' 'unsafe-eval' 'unsafe-inline'; " +
            "style-src   * 'self' 'unsafe-inline'; " +
            "frame-ancestors 'none';");
        return true;
    }
    
}
