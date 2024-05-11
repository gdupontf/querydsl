package io.github.openfeign.querydsl.boot;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.Hibernate5Templates;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.r2dbc.R2DBCConnectionProvider;
import com.querydsl.r2dbc.R2DBCQueryFactory;
import com.querydsl.sql.*;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.spring.QueryDSLSQLTemplatesCustomizer;
import com.querydsl.sql.spring.QueryDSLSqlConfigurationCustomizer;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import io.github.openfeign.querydsl.jpa.spring.repository.JPQLRepository;
import io.github.openfeign.querydsl.jpa.spring.repository.config.EnableQuerydslRepositories;
import io.github.openfeign.querydsl.jpa.spring.repository.support.QueryDslConfigExtension;
import io.github.openfeign.querydsl.jpa.spring.repository.support.QuerydslJpaRepositoryFactoryBean;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import java.lang.annotation.*;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import reactor.core.publisher.Mono;

@org.springframework.context.annotation.Configuration
public class QueryDSLAutoConfiguration {

  public static class QueryDSLProperties {
    private final Boolean printSchema;
    private final Boolean quote;
    private final Boolean newLineToSingleSpace;
    private final Character escapeCharacter;

    public QueryDSLProperties(
        boolean printSchema, boolean quote, boolean newLineToSingleSpace, char escapeCharacter) {
      this.printSchema = printSchema;
      this.quote = quote;
      this.newLineToSingleSpace = newLineToSingleSpace;
      this.escapeCharacter = escapeCharacter;
    }

    public Boolean getPrintSchema() {
      return printSchema;
    }

    public Boolean getQuote() {
      return quote;
    }

    public Boolean getNewLineToSingleSpace() {
      return newLineToSingleSpace;
    }

    public Character getEscapeCharacter() {
      return escapeCharacter;
    }
  }

  @org.springframework.context.annotation.Configuration
  @ConditionalOnClass(SpringConnectionProvider.class)
  @ConditionalOnBean(DataSource.class)
  @EnableConfigurationProperties(SQLConfiguration.SQLProperties.class)
  public static class SQLConfiguration {

    @Bean
    public SpringConnectionProvider connectionProvider(DataSource dataSource) {
      return new SpringConnectionProvider(dataSource);
    }

    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration(
        SQLTemplates templates, ObjectProvider<QueryDSLSqlConfigurationCustomizer> objectProvider) {
      Configuration.Builder builder = Configuration.Builder.builder(templates);
      objectProvider.forEach(customizer -> customizer.customize(builder));
      return builder.build();
    }

    @Bean
    public SQLQueryFactory queryFactory(
        SpringConnectionProvider connectionProvider, com.querydsl.sql.Configuration configuration) {
      return new SQLQueryFactory(configuration, connectionProvider);
    }

    @Bean
    public SpringConnectionProvider springConnectionProvider(DataSource dataSource) {
      return new SpringConnectionProvider(dataSource);
    }

    @Bean
    public SQLTemplates sqlTemplatesBuilder(
        DataSource dataSource, ObjectProvider<QueryDSLSQLTemplatesCustomizer> customizers)
        throws MetaDataAccessException {
      DatabaseDriver databaseDriver =
          DatabaseDriver.fromJdbcUrl(
              JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getURL));
      SQLTemplates.Builder builder =
          switch (databaseDriver) {
            case DB2:
              yield DB2Templates.builder();
            case DERBY:
              yield DerbyTemplates.builder();
            case H2:
              yield H2Templates.builder();
            case HSQLDB:
              yield HSQLDBTemplates.builder();
            case SQLITE:
              yield SQLiteTemplates.builder();
            case MYSQL:
              yield MySQLTemplates.builder();
            case ORACLE:
              yield OracleTemplates.builder();
            case POSTGRESQL:
              yield PostgreSQLTemplates.builder();
            case SQLSERVER:
              int major =
                  JdbcUtils.extractDatabaseMetaData(
                      dataSource, DatabaseMetaData::getDatabaseMajorVersion);
              if (major >= 9) {
                if (major >= 10) {
                  if (major >= 11) {
                    yield SQLServer2012Templates.builder();
                  }
                  yield SQLServer2008Templates.builder();
                }
                yield SQLServer2005Templates.builder();
              }
              // default to plain implementation
              yield SQLServerTemplates.builder();
            case FIREBIRD:
              yield FirebirdTemplates.builder();
            default:
              throw new UnsupportedDatabaseTypeException(
                  "Unsupported database type : %s".formatted(databaseDriver));
          };
      customizers.forEach(customizer -> customizer.customize(builder));
      return builder.build();
    }

    @Bean
    public QueryDSLSqlConfigurationCustomizer addSpringExceptionTranslator(
        SpringExceptionTranslator exceptionTranslator) {
      return builder -> builder.exceptionTranslator(exceptionTranslator);
    }

    @Bean
    public SpringExceptionTranslator springExceptionTranslator() {
      return new SpringExceptionTranslator();
    }

    @Bean
    public QueryDSLSqlConfigurationCustomizer registerListenerBeans(
        ObjectProvider<SQLListener> listeners) {
      return configuration -> listeners.forEach(configuration::listener);
    }

    @ConfigurationProperties("querydsl.sql")
    public static class SQLProperties extends QueryDSLProperties
        implements QueryDSLSQLTemplatesCustomizer, QueryDSLSqlConfigurationCustomizer {

      private final Integer maxFieldSize;
      private final Integer maxRows;
      private final Integer queryTimeout;
      private final Integer fetchSize;

      @ConstructorBinding
      public SQLProperties(
          boolean printSchema,
          boolean quote,
          boolean newLineToSingleSpace,
          char escapeCharacter,
          Integer maxFieldSize,
          Integer maxRows,
          Integer queryTimeout,
          Integer fetchSize) {
        super(printSchema, quote, newLineToSingleSpace, escapeCharacter);
        this.maxFieldSize = maxFieldSize;
        this.maxRows = maxRows;
        this.queryTimeout = queryTimeout;
        this.fetchSize = fetchSize;
      }

      @Override
      public void customize(SQLTemplates.Builder builder) {
        if (getQuote() != null) {
          builder.quote(getQuote());
        }
        if (getPrintSchema() != null) {
          builder.printSchema(getPrintSchema());
        }
        if (getNewLineToSingleSpace() != null) {
          builder.newLineToSingleSpace(getNewLineToSingleSpace());
        }
        if (getEscapeCharacter() != null) {
          builder.escape(getEscapeCharacter());
        }
      }

      @Override
      public void customize(Configuration.Builder builder) {
        if (getMaxFieldSize() != null) {
          builder.maxFieldSize(getMaxFieldSize());
        }
        if (getFetchSize() != null) {
          builder.fetchSize(getFetchSize());
        }
        if (getQueryTimeout() != null) {
          builder.queryTimeout(getQueryTimeout());
        }
        if (getMaxRows() != null) {
          builder.maxRows(getMaxRows());
        }
      }

      public Integer getMaxFieldSize() {
        return maxFieldSize;
      }

      public Integer getMaxRows() {
        return maxRows;
      }

      public Integer getQueryTimeout() {
        return queryTimeout;
      }

      public Integer getFetchSize() {
        return fetchSize;
      }
    }
  }

  @AutoConfiguration(after = {HibernateJpaAutoConfiguration.class})
  @ConditionalOnBean(JPQLQueryFactory.class)
  @ConditionalOnClass(JPQLRepository.class)
  @ConditionalOnMissingBean({QuerydslJpaRepositoryFactoryBean.class})
  @ConditionalOnProperty(
      prefix = "spring.data.jpa.repositories",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  @Import(JPARepositoriesConfiguration.QueryDSLJpaRepositoriesImportSelector.class)
  public static class JPARepositoriesConfiguration {

    static class QueryDSLJpaRepositoriesImportSelector implements ImportSelector {

      @Override
      public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {QueryDSLJpaRepositoriesRegistrar.class.getName()};
      }
    }

    static class QueryDSLJpaRepositoriesRegistrar
        extends AbstractRepositoryConfigurationSourceSupport {

      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return EnableQuerydslRepositories.class;
      }

      @Override
      protected Class<?> getConfiguration() {
        return EnableQueryDSLRepositoriesConfiguration.class;
      }

      @Override
      protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new QueryDslConfigExtension();
      }

      @EnableQuerydslRepositories
      private static final class EnableQueryDSLRepositoriesConfiguration {}
    }
  }

  @AutoConfiguration(after = {HibernateJpaAutoConfiguration.class})
  @ConditionalOnClass({JPQLQueryFactory.class, JpaVendorAdapter.class})
  @ConditionalOnBean(EntityManagerFactory.class)
  public static class JPAConfiguration {

    @Bean
    @ConditionalOnBean(HibernateJpaVendorAdapter.class)
    public JPQLQueryFactory jpqlQueryFactory(
        JPQLTemplates templates, EntityManagerFactory entityManagerFactory) {
      SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
      return new HibernateQueryFactory(templates, sessionFactory::getCurrentSession);
    }

    @Bean
    @ConditionalOnMissingBean
    public JPQLQueryFactory genericJpQLQueryFactory(
        JPQLTemplates templates, EntityManagerFactory entityManagerFactory) {
      return new JPAQueryFactory(templates, entityManagerFactory::createEntityManager);
    }

    @Bean
    @ConditionalOnBean(HibernateJpaVendorAdapter.class)
    public Hibernate5Templates hibernateJpqlTemplates() {
      return new Hibernate5Templates();
    }

    @Bean
    @ConditionalOnBean(EclipseLinkJpaVendorAdapter.class)
    public EclipseLinkTemplates eclipseLinkJpqlTemplates() {
      return new EclipseLinkTemplates();
    }
  }

  @org.springframework.context.annotation.Configuration
  @ConditionalOnBean(ConnectionFactory.class)
  @EnableConfigurationProperties(R2DBCConfiguration.R2DBCQueryDSLProperties.class)
  public static class R2DBCConfiguration {

    @Bean
    public R2DBCQueryFactory queryFactory(
        com.querydsl.r2dbc.SQLTemplates templates, ConnectionFactory connectionFactory) {
      return new R2DBCQueryFactory(templates, () -> Mono.from(connectionFactory.create()));
    }

    @Bean
    public R2DBCConnectionProvider connectionProvider(ConnectionFactory connectionFactory) {
      return () -> Mono.from(connectionFactory.create());
    }

    @ConfigurationProperties("spring.querydsl")
    public static class R2DBCQueryDSLProperties extends QueryDSLProperties
        implements R2DBCSQLTemplatesBuilderCustomizer {
      public R2DBCQueryDSLProperties(
          boolean printSchema, boolean quote, boolean newLineToSingleSpace, char escapeCharacter) {
        super(printSchema, quote, newLineToSingleSpace, escapeCharacter);
      }

      @Override
      public void customize(com.querydsl.r2dbc.SQLTemplates.Builder builder) {
        if (getPrintSchema() != null) {
          builder.printSchema(getPrintSchema());
        }
        if (getQuote() != null) {
          builder.quote(getQuote());
        }
        if (getNewLineToSingleSpace() != null) {
          builder.newLineToSingleSpace(getNewLineToSingleSpace());
        }
        if (getEscapeCharacter() != null) {
          builder.escape(getEscapeCharacter());
        }
      }
    }

    public interface R2DBCSQLTemplatesBuilderCustomizer {
      void customize(com.querydsl.r2dbc.SQLTemplates.Builder builder);
    }
  }

  public static class UnsupportedDatabaseTypeException extends RuntimeException {
    public UnsupportedDatabaseTypeException(String message) {
      super(message);
    }
  }
}
