package com.techcourse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.org.springframework.http.HttpStatus;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.mvc.tobe.AnnotationHandlerMapping;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerExecution;
import webmvc.org.springframework.web.servlet.view.JspView;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private HandlerMappings handlerMappings;

    public DispatcherServlet() {
    }

    @Override
    public void init() {
        handlerMappings = new HandlerMappings();
        handlerMappings.addHandlerMapping(new AnnotationHandlerMapping("com.techcourse.controller"));
        handlerMappings.addHandlerMapping(new ManualHandlerMapping());
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        log.debug("Method : {}, Request URI : {}", request.getMethod(), request.getRequestURI());

        try {
            Optional<HandlerExecution> execution  = handlerMappings.getHandler(request);

            if (execution.isEmpty()) {
                response.setStatus(HttpStatus.NOT_FOUND.getValue());
                return;
            }

            ModelAndView view = execution.get().handle(request, response);
            move(view.getViewName(), request, response);
        } catch (Throwable e) {
            log.error("Exception : {}", e.getMessage(), e);
            throw new ServletException(e.getMessage());
        }
    }

    private void move(
            String viewName,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        if (viewName.startsWith(JspView.REDIRECT_PREFIX)) {
            response.sendRedirect(viewName.substring(JspView.REDIRECT_PREFIX.length()));
            return;
        }

        final var requestDispatcher = request.getRequestDispatcher(viewName);
        requestDispatcher.forward(request, response);
    }

}
