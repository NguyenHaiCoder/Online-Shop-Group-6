package com.swd392.onlineshop.config;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.web.IWebRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Thymeleaf 3.1 bo #request mac dinh. Template van dung requestURI va queryString.
 */
@Component
public class WebExpressionDialect extends AbstractDialect implements IExpressionObjectDialect {

    public WebExpressionDialect() {
        super("web-request");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new Factory();
    }

    private static final class Factory implements IExpressionObjectFactory {

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return Set.of("request");
        }

        @Override
        public boolean isCacheable(String expressionObjectName) {
            return false;
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            if (!"request".equals(expressionObjectName) || !(context instanceof IWebContext web)) {
                return null;
            }
            return new RequestView(web.getExchange().getRequest());
        }
    }

    public static final class RequestView {

        private final IWebRequest request;

        private RequestView(IWebRequest request) {
            this.request = request;
        }

        public String getRequestURI() {
            String path = request.getPathWithinApplication();
            return path == null || path.isEmpty() ? "/" : path;
        }

        public String getQueryString() {
            return request.getQueryString();
        }
    }
}
