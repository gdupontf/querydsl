package com.querydsl.sql.spring.event;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLMergeUsingCase;
import com.querydsl.sql.dml.SQLUpdateBatch;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class QueryDslEventListenerAdapter implements SQLListener, ApplicationEventPublisherAware {

  ApplicationEventPublisher eventPublisher;

  @Override
  public void notifyQuery(QueryMetadata md) {
    eventPublisher.publishEvent(new QueryEvent(this, md));
  }

  @Override
  public void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
    eventPublisher.publishEvent(new DeleteEvent(this, entity, md));
  }

  @Override
  public void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
    eventPublisher.publishEvent(new DeletesEvent(this, entity, batches));
  }

  @Override
  public void notifyMerge(
      RelationalPath<?> entity,
      QueryMetadata md,
      List<Path<?>> keys,
      List<Path<?>> columns,
      List<Expression<?>> values,
      SubQueryExpression<?> subQuery) {
    eventPublisher.publishEvent(new MergeEvent(this, entity, md, keys, columns, values, subQuery));
  }

  @Override
  public void notifyMerges(
      RelationalPath<?> entity, QueryMetadata md, List<SQLMergeBatch> batches) {
    eventPublisher.publishEvent(new MergesEvent(this, entity, md, batches));
  }

  @Override
  public void notifyMergeUsing(
      RelationalPath<?> entity,
      QueryMetadata md,
      SimpleExpression<?> usingExpression,
      Predicate usingOn,
      List<SQLMergeUsingCase> whens) {
    eventPublisher.publishEvent(
        new MergeUsingEvent(this, entity, md, usingExpression, usingOn, whens));
  }

  @Override
  public void notifyInsert(
      RelationalPath<?> entity,
      QueryMetadata md,
      List<Path<?>> columns,
      List<Expression<?>> values,
      SubQueryExpression<?> subQuery) {
    eventPublisher.publishEvent(new InsertEvent(this, entity, md, columns, values, subQuery));
  }

  @Override
  public void notifyInserts(
      RelationalPath<?> entity, QueryMetadata md, List<SQLInsertBatch> batches) {
    eventPublisher.publishEvent(new InsertsEvent(this, entity, md, batches));
  }

  @Override
  public void notifyUpdate(
      RelationalPath<?> entity, QueryMetadata md, Map<Path<?>, Expression<?>> updates) {
    eventPublisher.publishEvent(new UpdateEvent(this, entity, md, updates));
  }

  @Override
  public void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
    eventPublisher.publishEvent(new UpdatesEvent(this, entity, batches));
  }

  @Override
  public void setApplicationEventPublisher(
      @NotNull ApplicationEventPublisher applicationEventPublisher) {
    this.eventPublisher = applicationEventPublisher;
  }

  public static final class DeleteEvent extends QueryDSLApplicationEvent {
    public DeleteEvent(Object source, RelationalPath<?> entity, QueryMetadata metadata) {
      super(source, entity, metadata);
    }
  }

  public static final class QueryEvent extends QueryDSLApplicationEvent {
    public QueryEvent(Object source, QueryMetadata metadata) {
      super(source, null, metadata);
    }
  }

  public static final class DeletesEvent extends QueryDSLApplicationEvent {

    private final List<QueryMetadata> batches;

    public DeletesEvent(Object source, RelationalPath<?> entity, List<QueryMetadata> batches) {
      super(source, entity, null);
      this.batches = batches;
    }

    public List<QueryMetadata> getBatches() {
      return batches;
    }
  }

  public static final class MergeEvent extends QueryDSLApplicationEvent {

    public final List<Path<?>> keys;
    public final List<Path<?>> columns;
    public final List<Expression<?>> values;
    public final SubQueryExpression<?> subQuery;

    public MergeEvent(
        Object source,
        RelationalPath<?> entity,
        QueryMetadata metadata,
        List<Path<?>> keys,
        List<Path<?>> columns,
        List<Expression<?>> values,
        SubQueryExpression<?> subQuery) {
      super(source, entity, metadata);
      this.keys = keys;
      this.columns = columns;
      this.values = values;
      this.subQuery = subQuery;
    }
  }

  public static final class MergesEvent extends QueryDSLApplicationEvent {

    private final List<SQLMergeBatch> batches;

    public MergesEvent(
        Object source,
        RelationalPath<?> entity,
        QueryMetadata metadata,
        List<SQLMergeBatch> batches) {
      super(source, entity, metadata);
      this.batches = batches;
    }

    public List<SQLMergeBatch> getBatches() {
      return batches;
    }
  }

  public static final class MergeUsingEvent extends QueryDSLApplicationEvent {

    private final SimpleExpression<?> usingExpression;
    private final Predicate usingOn;
    private final List<SQLMergeUsingCase> whens;

    public MergeUsingEvent(
        Object source,
        RelationalPath<?> entity,
        QueryMetadata metadata,
        SimpleExpression<?> usingExpression,
        Predicate usingOn,
        List<SQLMergeUsingCase> whens) {
      super(source, entity, metadata);
      this.usingExpression = usingExpression;
      this.usingOn = usingOn;
      this.whens = whens;
    }

    public SimpleExpression<?> getUsingExpression() {
      return usingExpression;
    }

    public Predicate getUsingOn() {
      return usingOn;
    }

    public List<SQLMergeUsingCase> getWhens() {
      return whens;
    }
  }

  public static final class InsertEvent extends QueryDSLApplicationEvent {

    private final List<Path<?>> columns;
    private final List<Expression<?>> values;
    private final SubQueryExpression<?> subQuery;

    public InsertEvent(
        Object source,
        RelationalPath<?> entity,
        QueryMetadata metadata,
        List<Path<?>> columns,
        List<Expression<?>> values,
        SubQueryExpression<?> subQuery) {
      super(source, entity, metadata);
      this.columns = columns;
      this.values = values;
      this.subQuery = subQuery;
    }

    public List<Path<?>> getColumns() {
      return columns;
    }

    public List<Expression<?>> getValues() {
      return values;
    }

    public SubQueryExpression<?> getSubQuery() {
      return subQuery;
    }
  }

  public static final class InsertsEvent extends QueryDSLApplicationEvent {

    private final List<SQLInsertBatch> batches;

    public InsertsEvent(
        Object source,
        RelationalPath<?> entity,
        QueryMetadata metadata,
        List<SQLInsertBatch> batches) {
      super(source, entity, metadata);
      this.batches = batches;
    }

    public List<SQLInsertBatch> getBatches() {
      return batches;
    }
  }

  public static final class UpdateEvent extends QueryDSLApplicationEvent {

    private final Map<Path<?>, Expression<?>> updates;

    public UpdateEvent(
        Object source,
        RelationalPath<?> entity,
        QueryMetadata metadata,
        Map<Path<?>, Expression<?>> updates) {
      super(source, entity, metadata);
      this.updates = updates;
    }

    public Map<Path<?>, Expression<?>> getUpdates() {
      return updates;
    }
  }

  public static final class UpdatesEvent extends QueryDSLApplicationEvent {
    private final List<SQLUpdateBatch> batches;

    public UpdatesEvent(Object source, RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
      super(source, entity, null);
      this.batches = batches;
    }

    public List<SQLUpdateBatch> getBatches() {
      return batches;
    }
  }

  public abstract static sealed class QueryDSLApplicationEvent extends ApplicationEvent
      permits DeleteEvent,
          DeletesEvent,
          InsertEvent,
          InsertsEvent,
          MergeEvent,
          MergeUsingEvent,
          MergesEvent,
          QueryEvent,
          UpdateEvent,
          UpdatesEvent {

    private final RelationalPath<?> entity;
    private final QueryMetadata metadata;

    public QueryDSLApplicationEvent(
        Object source, RelationalPath<?> entity, QueryMetadata metadata) {
      super(source);
      this.entity = entity;
      this.metadata = metadata;
    }

    public RelationalPath<?> getEntity() {
      return entity;
    }

    public QueryMetadata getMetadata() {
      return metadata;
    }
  }
}
