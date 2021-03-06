package com.siyeh.igtest.logging.log_statement_guarded_by_log_condition;

public class LogStatementGuardedByLogCondition {

    private static final Logger LOG = Logger.getLogger("log");

    void guarded(Object object) {
        if (LOG.isDebug()) {
            if (true) {
                if (true) {
                    LOG.debug("really expensive logging" + object);
                }
            }
        }
    }

    void unguarded(Object object) {
        if (true) {
            if (true) {
                LOG.debug("log log log " + object);
            }
        }
    }

    void wrondGuard(Object object) {
        if (LOG.isTrace()) {
            if (true) {
                if (true) {
                    LOG.debug("really expensive logging" + object);
                }
            }
        }
    }

  static class Logger {

    public Logger(String log) {
    }

    public void debug(String s) {
    }

    public void trace(String s) {}

    public boolean isDebug() {
      return true;
    }

    public boolean isTrace() {
      return true;
    }

    public static Logger getLogger(String log) {
      return new Logger(log);
    }
  }

}
