package com.typhoon.cempaka.util;

import java.util.EnumMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.abissell.javautil.io.ThreadLocalFormat;
import com.typhoon.fixswap.AvgPx;
import com.typhoon.fixswap.CcyPair;
import com.typhoon.fixswap.ClOrdrID;
import com.typhoon.fixswap.CumQty;
import com.typhoon.fixswap.ExecID;
import com.typhoon.fixswap.ExecType;
import com.typhoon.fixswap.FixField;
import com.typhoon.fixswap.FixFieldVal;
import com.typhoon.fixswap.LastPx;
import com.typhoon.fixswap.LastShares;
import com.typhoon.fixswap.OrdStatus;
import com.typhoon.fixswap.OrderQty;
import com.typhoon.fixswap.OrigClOrdrID;
import com.typhoon.fixswap.Price;
import com.typhoon.fixswap.Side;
import com.typhoon.fixswap.TimeInForce;

public enum Log {
    TRACE(Level.TRACE) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.trace(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.trace(msg, throwable);
        }
    },
    DEBUG(Level.DEBUG) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.debug(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.debug(msg, throwable);
        }
    },
    INFO(Level.INFO) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.info(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.info(msg, throwable);
        }
    },
    WARN(Level.WARN) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.warn(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.warn(msg, throwable);
        }
    },
    ERROR(Level.ERROR) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.error(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.error(msg, throwable);
        }
    },
    FATAL(Level.FATAL) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.fatal(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.fatal(msg, throwable);
        }
    };

    abstract void toLogger(Logger logger, String msg);
    abstract void toLogger(Logger logger, String msg, Throwable throwable);

    public void to(LogDst dst, String msg) {
        var log = dst.getLogger();
        if (isEnabled(log)) {
            toLogger(log, msg);
        }
    }

    public void to(LogDst dst, String prefix, String msg) {
        if (isEnabled(dst)) {
            to(dst, prefix + msg);
        }
    }

    public final boolean isEnabled(Logger logger) {
        return logger.isEnabled(level);
    }

    public boolean isEnabled(LogDst dst) {
        return isEnabled(dst.getLogger());
    }

    public boolean isEnabled(LogDstSet<?> dstSet) {
        for (LogDst dst : dstSet.set()) {
            var logger = dst.getLogger();
            if (isEnabled(logger)) {
                return true;
            }
        }

        return false;
    }

    private final Level level;

    Log(Level level) {
        this.level = level;
    }

    public void to(LogDstSet<?> dstSet, String prefix, String msg) {
        if (isEnabled(dstSet)) {
            var fullMsg = prefix + msg;
            to(dstSet, fullMsg);
        }
    }

    public void to(LogDstSet<?> dstSet, String prefix, Object obj) {
        if (isEnabled(dstSet)) {
            to(dstSet, prefix, obj.toString());
        }
    }

    public void to(LogDstSet<?> dstSet, String prefix, Supplier<String> msgSupplier) {
        if (isEnabled(dstSet)) {
            to(dstSet, prefix, msgSupplier.get());
        }
    }

    public void to(LogDstSet<?> dstSet, Supplier<String> msgSupplier) {
        to(dstSet, "", msgSupplier);
    }

    public void to(LogDstSet<?> dstSet, Object obj) {
        if (isEnabled(dstSet)) {
            to(dstSet, obj.toString());
        }
    }

    public void to(LogDstSet<?> dstSet, String msg) {
        if (isEnabled(dstSet)) {
            dstSet.set().forEach(dst -> to(dst, msg));
        }
    }

    public void to(LogDstSet<?> dstSet, String prefix, EnumMap<FixField, FixFieldVal> fixFields) {
        if (isEnabled(dstSet)) {
            dstSet.set().forEach(dst ->
                fixFields.forEach((__, v) -> to(dst, prefix + fixLogline(v)))
            );
        }
    }

    public String fixLogline(FixFieldVal val) {
        int fixTag = val.fixTag();
        if (fixTag >= 0) {
            return val.fixName() + " <" + val.fixTag() + ">: " + val;
        } else {
            return val.fixName() + ": " + val;
        }
    }

    public static void logExecutionReport(EnumMap<FixField, FixFieldVal> execReport,
            OptBuf buf) {
        if (buf instanceof OptBuf.Noop) {
            return;
        }

        CcyPair ccyPair = (CcyPair) execReport.get(FixField.SYMBOL);
        buf.add("\n").add(ccyPair).add(" ");
        ExecType execType = (ExecType) execReport.get(FixField.EXEC_TYPE);
        buf.add(getType(execType)).add(" ");
        Side side = (Side) execReport.get(FixField.SIDE);
        buf.add(getAction(execType, side)).add(" ");
        buf.add(getQty(execType, execReport)).add(" @ ");
        buf.add(getPx(execType, execReport));
        OrdStatus status = (OrdStatus) execReport.get(FixField.ORD_STATUS);
        if (status != null) {
            buf.add(" | STATUS: ").add(status);
        }
        TimeInForce tif = (TimeInForce) execReport.get(FixField.TIME_IN_FORCE);
        if (tif != null) {
            buf.add(" | TIF: ").add(tif);
        }
        buf.add(" | TS: ").add(execReport.get(FixField.TRANSACT_TIME));
        buf.add(" | ORD_ID: ").add(((ClOrdrID) execReport.get(FixField.CL_ORD_ID)).fieldVal());
        CumQty cumQty = (CumQty) execReport.get(FixField.CUM_QTY);
        if (cumQty != null) {
            buf.add(" | CUM_QTY: ").add(cumQty.decimalString());
        }
        AvgPx avgPx = (AvgPx) execReport.get(FixField.AVG_PX);
        if (avgPx != null) {
            buf.add(" | AVG_PX: ").add(avgPx.decimalString());
        }
        OrigClOrdrID origID = (OrigClOrdrID) execReport.get(FixField.ORIG_CL_ORD_ID);
        if (origID != null) {
            buf.add(" | ORIG_ORD_ID: ").add(origID.fieldVal());
        }
        ExecID execID = (ExecID) execReport.get(FixField.EXEC_ID);
        if (execID != null) {
            buf.add(" | EXEC_ID: ").add(execID.fieldVal());
        }
    }

    private static String getType(ExecType execType) {
        return switch (execType) {
            case PARTIAL_FILL -> "FILL";
            case CANCELED -> "CXLD";
            case PENDING_CANCEL -> "PEND_CXL";
            case PENDING_NEW -> "PEND_NEW";
            default -> execType.toString();
        };
    }

    private static String getAction(ExecType type, Side side) {
        return switch (type) {
            case PENDING_NEW, NEW, PENDING_CANCEL, CANCELED, REJECTED -> {
                yield switch (side) {
                    case BUY -> "BID";
                    case SELL -> "ASK";
                    default -> throw new IllegalArgumentException("" + side);
                };
            }
            case PARTIAL_FILL, FILL -> {
                yield switch (side) {
                    case BUY -> "BOT";
                    case SELL -> "SLD";
                    default -> throw new IllegalArgumentException("" + side);
                };
            }
            default -> throw new IllegalArgumentException("" + type);
        };
    }

    private static String getQty(ExecType type, EnumMap<FixField, FixFieldVal> msg) {
        return switch (type) {
            case PARTIAL_FILL, FILL -> ((LastShares) msg.get(FixField.LAST_SHARES)).decimalString();
            case PENDING_CANCEL, CANCELED -> {
                CumQty cumQty = (CumQty) msg.get(FixField.CUM_QTY);
                double cumQtyVal;
                if (cumQty != null) {
                    cumQtyVal = cumQty.fieldVal();
                } else {
                    cumQtyVal = 0.0d;
                }
                OrderQty orderQty = (OrderQty) msg.get(FixField.ORDER_QTY);
                double cxldVal = orderQty.fieldVal() - cumQtyVal;
                yield ThreadLocalFormat.with8SigDigits().format(cxldVal);
            }
            default -> ((OrderQty) msg.get(FixField.ORDER_QTY)).decimalString();
        };
    }

    private static String getPx(ExecType type, EnumMap<FixField, FixFieldVal> msg) {
        return switch (type) {
            case PARTIAL_FILL, FILL -> ((LastPx) msg.get(FixField.LAST_PX)).decimalString();
            default -> ((Price) msg.get(FixField.PRICE)).decimalString();
        };
    }
}
