package woo.spring.cache.common.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class CustomCacheKeyGenerator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * @return {cacheStratagy}:{cacheName}:{key}
     */
    public String genKey(JoinPoint joinPoint, CacheStrategy cacheStratagy, String cacheName, String keySpel) {
        EvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int ii = 0; ii < args.length; ii++) {
            context.setVariable(parameterNames[ii], args[ii]);
        }

        return cacheStratagy + ":" + cacheName + ":" + parser.parseExpression(keySpel).getValue(context, String.class);
    }

}
