package webmvc.org.springframework.web.servlet.mvc.tobe;

import context.org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.org.springframework.web.bind.annotation.RequestMapping;
import web.org.springframework.web.bind.annotation.RequestMethod;

public class AnnotationHandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackages;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackages) {
        this.basePackages = basePackages;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        log.info("Initialized AnnotationHandlerMapping!");

        Set<Class<?>> classes = new Reflections(basePackages)
                .getTypesAnnotatedWith(Controller.class);

        putHandlerExecutionsByController(classes);
    }

    private void putHandlerExecutionsByController(Set<Class<?>> classes) {
        try {
            for (Class<?> clazz : classes) {
                Object controller = clazz.getConstructor().newInstance();
                List<Method> methods = Arrays.stream(clazz.getMethods())
                        .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                        .collect(Collectors.toList());
                putHandlerExecutionByMethod(controller, methods);
            }
        } catch (Exception exception) {
            log.error("예외 발생 {0}", exception);
        }
    }

    private void putHandlerExecutionByMethod(Object controller, List<Method> declaredMethods) {
        for (Method method : declaredMethods) {
            putHandlerExecutionByAnnotation(controller, method);
        }
    }

    private void putHandlerExecutionByAnnotation(Object controller, Method method) {
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);

        for (RequestMethod requestMethod : annotation.method()) {
            HandlerKey handlerKey = new HandlerKey(annotation.value(), requestMethod);
            HandlerExecution handlerExecution = new HandlerExecution(controller, method);
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    public Object getHandler(final HttpServletRequest request) {
        HandlerKey key = new HandlerKey(
                request.getRequestURI(),
                RequestMethod.from(request.getMethod())
        );

        return handlerExecutions.computeIfAbsent(
                key,
                ignored -> {
                    throw new IllegalArgumentException("올바르지 않은 요청입니다.");
                }
        );
    }

}
