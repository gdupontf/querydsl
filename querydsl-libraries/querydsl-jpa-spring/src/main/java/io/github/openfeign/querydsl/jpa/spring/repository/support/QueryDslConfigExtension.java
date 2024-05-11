package io.github.openfeign.querydsl.jpa.spring.repository.support;

import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension.JpaRepositoryRegistrationAotProcessor;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

public class QueryDslConfigExtension extends RepositoryConfigurationExtensionSupport {
  @Override
  protected String getModulePrefix() {
    return "QueryDSL";
  }

  @Override
  public String getModuleIdentifier() {
    return "QueryDSL";
  }

  @Override
  public Class<? extends BeanRegistrationAotProcessor> getRepositoryAotProcessor() {
    return JpaRepositoryRegistrationAotProcessor.class;
  }

  @Override
  public String getRepositoryFactoryBeanClassName() {
    return QuerydslJpaRepositoryFactoryBean.class.getName();
  }
}
