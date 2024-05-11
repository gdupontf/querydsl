package com.querydsl.sql.spring;

import com.querydsl.sql.Configuration;

@FunctionalInterface
public interface QueryDSLSqlConfigurationCustomizer {
  void customize(Configuration.Builder configuration);
}
