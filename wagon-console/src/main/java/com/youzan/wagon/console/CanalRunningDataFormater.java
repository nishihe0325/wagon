package com.youzan.wagon.console;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.alibaba.otter.canal.extend.common.bean.CanalInstanceRunningData;
import com.alibaba.otter.canal.protocol.position.EntryPosition;

/**
 * @author wangguofeng since 2016年4月19日 上午9:30:21
 */
public class CanalRunningDataFormater {

    private static CanalRunningDataFormater instance;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CanalRunningDataFormater() {
    }

    public static CanalRunningDataFormater buildInstance() {
        if (instance == null) {
            instance = new CanalRunningDataFormater();
        }

        return instance;
    }

    // ====================== position ====================

    public String dumpDateTime(CanalInstanceRunningData data) {
        if (data != null) {
            return dateTime(data.getDumpPosition());
        }
        return "";
    }

    public String consumePosition(CanalInstanceRunningData data) {
        EntryPosition entryPosition = getConsumePosition(data);
        if (entryPosition != null) {
            return entryPosition.getJournalName() + ":" + entryPosition.getPosition();
        }
        return "";
    }

    public String consumeDateTime(CanalInstanceRunningData data) {
        if (data != null) {
            return dateTime(getConsumePosition(data));
        }
        return "";
    }

    // ====================== buffer ===================
    public String bufferSizeRemain(CanalInstanceRunningData data) {
        if (data != null) {
            return formatNumber(data.getBufferSizeRemain());
        }
        return "";
    }

    public String bufferCapacityRemain(CanalInstanceRunningData data) {
        if (data != null) {
            return formatNumber(data.getBufferCapacityRemain());
        }
        return "";
    }

    // ====================== delay ====================

    public boolean isDelay(CanalInstanceRunningData data, long delayTime) {
        Long consumeDelay = getConsumeDelay(data);
        return consumeDelay == null ? false : ((consumeDelay >= delayTime) ? true : false);
    }

    public String delayDes(CanalInstanceRunningData data) {
        Long consumeDelay = getConsumeDelay(data);
        return consumeDelay == null ? "" : delayDes(consumeDelay);
    }

    // ====================== help method ====================
    private String dateTime(EntryPosition entryPosition) {
        if (entryPosition != null) {
            Long timestamp = entryPosition.getTimestamp();
            if (timestamp != null) {
                return dateFormat.format(new Date(timestamp));
            }
        }
        return "";
    }

    private EntryPosition getConsumePosition(CanalInstanceRunningData data) {
        if (data != null) {
            Map<String, EntryPosition> consumePositionTable = data.getConsumePositionTable();
            if (consumePositionTable != null) {
                return consumePositionTable.get(String.valueOf(1001));
            }
        }
        return null;
    }

    private Long getConsumeDelay(CanalInstanceRunningData data) {
        if (data != null) {
            Map<String, Long> consumeDelayTable = data.getConsumeDelayTable();
            if (consumeDelayTable != null) {
                return consumeDelayTable.get(String.valueOf(1001));
            }
        }
        return null;
    }

    private String delayDes(long delayInMills) {
        long days = delayInMills / (1000 * 60 * 60 * 24);
        long hour = (delayInMills / (60 * 60 * 1000) - days * 24);
        long min = ((delayInMills / (60 * 1000)) - days * 24 * 60 - hour * 60);
        long second = (delayInMills / 1000 - days * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

        StringBuilder buffer = new StringBuilder("");
        if (days > 0) {
            buffer.append(days).append("天");
        }

        if (hour > 0) {
            buffer.append(hour).append("时");
        } else if (buffer.length() > 0) {
            buffer.append("0时");
        }

        if (min > 0) {
            buffer.append(min).append("分");
        } else if (buffer.length() > 0) {
            buffer.append("0分");
        }

        if (second > 0) {
            buffer.append(second).append("秒");
        } else if (buffer.length() > 0) {
            buffer.append("0秒");
        }

        return buffer.toString();
    }

    public String formatNumber(long number) {
        DecimalFormat myformat = new DecimalFormat();
        myformat.applyPattern("##,###");
        return myformat.format(number);
    }

}
