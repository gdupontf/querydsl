package com.querydsl.sql.spring.event;

import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListenerContext;
import org.springframework.context.ApplicationEvent;

public class QueryDSLDetailedEventListenerAdapter extends QueryDslEventListenerAdapter
    implements SQLDetailedListener {

  @Override
  public void start(SQLListenerContext context) {
    eventPublisher.publishEvent(new StartEvent(this, context));
  }

  @Override
  public void preRender(SQLListenerContext context) {
    eventPublisher.publishEvent(new PreRenderEvent(this, context));
  }

  @Override
  public void rendered(SQLListenerContext context) {
    eventPublisher.publishEvent(new RenderedEvent(this, context));
  }

  @Override
  public void prePrepare(SQLListenerContext context) {
    eventPublisher.publishEvent(new PrePrepareEvent(this, context));
  }

  @Override
  public void prepared(SQLListenerContext context) {
    eventPublisher.publishEvent(new PreparedEvent(this, context));
  }

  @Override
  public void preExecute(SQLListenerContext context) {
    eventPublisher.publishEvent(new PreExecuteEvent(this, context));
  }

  public void executed(SQLListenerContext context) {
    eventPublisher.publishEvent(new ExecutedEvent(this, context));
  }

  @Override
  public void exception(SQLListenerContext context) {
    eventPublisher.publishEvent(new ErrorEvent(this, context));
  }

  @Override
  public void end(SQLListenerContext context) {
    eventPublisher.publishEvent(new EndEvent(this, context));
  }

  public static final class StartEvent extends QueryDSLDetailedEvent {
    public StartEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class PreRenderEvent extends QueryDSLDetailedEvent {
    public PreRenderEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class RenderedEvent extends QueryDSLDetailedEvent {
    public RenderedEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class PrePrepareEvent extends QueryDSLDetailedEvent {
    public PrePrepareEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class PreparedEvent extends QueryDSLDetailedEvent {
    public PreparedEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class PreExecuteEvent extends QueryDSLDetailedEvent {
    public PreExecuteEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class ExecutedEvent extends QueryDSLDetailedEvent {
    public ExecutedEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class ErrorEvent extends QueryDSLDetailedEvent {
    public ErrorEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public static final class EndEvent extends QueryDSLDetailedEvent {
    public EndEvent(Object source, SQLListenerContext context) {
      super(source, context);
    }
  }

  public abstract static sealed class QueryDSLDetailedEvent extends ApplicationEvent
      permits EndEvent,
          ErrorEvent,
          ExecutedEvent,
          PreExecuteEvent,
          PrePrepareEvent,
          PreRenderEvent,
          PreparedEvent,
          RenderedEvent,
          StartEvent {
    public QueryDSLDetailedEvent(Object source, SQLListenerContext context) {
      super(source);
    }
  }
}
