package com.querydsl.sql.spring;

import com.querydsl.sql.SQLTemplates;

@FunctionalInterface
public interface QueryDSLSQLTemplatesCustomizer {
  void customize(SQLTemplates.Builder builder);
}
