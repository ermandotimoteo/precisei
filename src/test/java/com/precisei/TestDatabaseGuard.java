package com.precisei;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Impede iniciar testes Spring contra a base pessoal ou operacional por engano. */
@Component
@Profile("test")
public class TestDatabaseGuard implements BeanFactoryPostProcessor, org.springframework.context.EnvironmentAware {
    private Environment env;
    @Override public void setEnvironment(Environment env) { this.env = env; }
    @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) {
        String url = env.getRequiredProperty("spring.datasource.url").split("\\?",2)[0];
        String database = url.substring(url.lastIndexOf('/')+1);
        if (!database.matches("[a-zA-Z0-9_]+_test(?:_[a-zA-Z0-9_]+)?")) {
            throw new IllegalStateException("Testes exigem nome de banco com sufixo _test ou _test_IDENTIFICADOR.");
        }
    }
}
