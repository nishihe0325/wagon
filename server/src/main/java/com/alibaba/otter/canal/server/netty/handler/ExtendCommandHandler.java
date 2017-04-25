package com.alibaba.otter.canal.server.netty.handler;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.extend.common.CmdExtendType;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchInstanceResult;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchRequestData;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchRequestData.DBData;
import com.alibaba.otter.canal.extend.common.bean.CanalDBSwitchResult;
import com.alibaba.otter.canal.extend.common.bean.CanalServerRunningData;
import com.alibaba.otter.canal.extend.common.bean.TablePositionData;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.CanalPacket.CmdExtend;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import com.alibaba.otter.canal.protocol.CanalPacket.PacketType;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty.NettyUtils;

/**
 * 扩展命令请求处理器
 * 
 * @author wangguofeng since 2016年1月14日 下午2:37:31
 */
public class ExtendCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExtendCommandHandler.class);

    private CanalServerWithEmbedded embeddedServer;
    private QueryCanalRunningInfoHandler queryCanalRunningInfoHandler;

    public ExtendCommandHandler(CanalServerWithEmbedded embeddedServer) {
        this.embeddedServer = embeddedServer;
        this.queryCanalRunningInfoHandler = new QueryCanalRunningInfoHandler(embeddedServer);
    }

    public void handleExtendCommand(CmdExtend cmd, ChannelHandlerContext ctx) {
        String cmdType = cmd.getCmdType();
        String cmdBody = cmd.getBody(); // json格式字符串
        logger.info("received extend command request, cmdType={}, cmdBody={}", cmdType, cmdBody);

        switch (cmdType) {
            case CmdExtendType.CANAL_DB_SWITCH: // canal db切换通知请求
                canalDbSwitch(cmdBody, ctx);
                break;
            case CmdExtendType.QUERY_CANAL_RUNNING_INFO: // 查询canal实例运行时信息命令请求
                queryCanalRunningInfo(cmdBody, ctx);
                break;
            case CmdExtendType.QUERY_TABLE_POSITION: // 查询表所在的mysql地址
                queryTablePosition(cmdBody, ctx);
                break;
            default:
                String errorMsg = String.format("extend command type=%s is not supported.", cmdType);
                logger.error(errorMsg);
                NettyUtils.error(400, errorMsg, ctx.getChannel(), null);
                break;
        }
    }

    // ========== different command type handle method =============

    private void canalDbSwitch(String cmdBody, ChannelHandlerContext ctx) {
        CanalDBSwitchResult result = null;
        try {
            result = doCanalDbSwitch(cmdBody, ctx);
        } catch (Exception e) {
            String resultMsg = "canal handle db swith goes wrong";
            logger.error(resultMsg + ", request:{}, \n{}", cmdBody, ExceptionUtils.getFullStackTrace(e));
            result = new CanalDBSwitchResult();
            result.setResultCode(CanalDBSwitchResult.RESULT_CODE_FAILED);
            result.setResultMsg(resultMsg + e.getMessage());
            return;
        }

        // 返回切换结果
        String bodyResp = result.toJson();
        logger.info("response canal db switch result:{}", bodyResp);
        CmdExtend cmdExtend = CmdExtend.newBuilder() //
                .setCmdType(CmdExtendType.CANAL_DB_SWITCH_RESULT) //
                .setBody(bodyResp).build();
        Packet packet = Packet.newBuilder()//
                .setType(PacketType.CMDEXTEND)//
                .setBody(cmdExtend.toByteString()).build();
        try {
            NettyUtils.write(ctx.getChannel(), packet.toByteArray(), null);
        } catch (Exception e) {
            logger.error("response canal db switch result goes wrong. request:{}, result:{}, \n{}", cmdBody, bodyResp, ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void queryCanalRunningInfo(String cmdBody, ChannelHandlerContext ctx) {
        // 获取运行信息
        CanalServerRunningData datas = queryCanalRunningInfoHandler.queryCanalRunningInfo(cmdBody);

        // 生成处理结果Packet对象，并返回给客户端
        String bodyResp = datas.toJson();
        logger.info("query canal running info result:{}", bodyResp);
        CmdExtend cmdExtend = CmdExtend.newBuilder() //
                .setCmdType(CmdExtendType.QUERY_CANAL_RUNNING_INFO_RESULT) //
                .setBody(bodyResp)//
                .build();
        Packet packet = Packet.newBuilder()//
                .setType(PacketType.CMDEXTEND)//
                .setBody(cmdExtend.toByteString())//
                .build();
        try {
            NettyUtils.write(ctx.getChannel(), packet.toByteArray(), null); // 返回处理结果
        } catch (Exception e) {
            logger.error("response query canal running info result failed, results: {}", bodyResp, ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void queryTablePosition(String cmdBody, ChannelHandlerContext ctx) {
        String bodyResp = null;
        try {
            String queryTableName = cmdBody;
            TablePositionData data = new TablePositionData(embeddedServer.getCanalServerWithNetty().getIp(), embeddedServer.getCanalServerWithNetty().getPort(), queryTableName);

            // 查询每个实例对应的mysql是否存在该表
            for (CanalInstance canalInstance : embeddedServer.getCanalInstances().values()) {
                MysqlEventParser eventParser = (MysqlEventParser) canalInstance.getEventParser();
                boolean exist = false;

                // 先从缓存中查询
                for (String tableFullName : eventParser.getTableMetaCache().getTableMetaCache().keySet()) {
                    if (!StringUtils.contains(queryTableName, ".") && queryTableName.equals(StringUtils.substringAfter(tableFullName, "."))//
                            && queryTableName.intern() == tableFullName.intern()) {
                        exist = true;
                        break; // 只要存在一条记录即可
                    }
                }

                // 缓存中不存在，则从mysql远程查找
                if (!exist) {
                    MysqlConnection connection = eventParser.getMetaConnection();
                    try {
                        exist = existTableInMysql(connection, queryTableName);
                    } catch (IOException e) {
                        // 尝试做一次retry操作
                        try {
                            connection.reconnect();
                            exist = existTableInMysql(connection, queryTableName);
                        } catch (IOException e1) {
                            throw new CanalParseException("fetch existTableInMysql failed, tableName:{}", queryTableName, e1);
                        }
                    }
                }

                // 存在则加入
                if (exist) {
                    data.addAddress(canalInstance.getDestination(), eventParser.getRunningInfo().getAddress());
                }
            }

            // 返回查询结果
            bodyResp = data.toJson();
            logger.info("response queryTablePosition result:{}", bodyResp);
            CmdExtend cmdExtend = CmdExtend.newBuilder() //
                    .setCmdType(CmdExtendType.QUERY_TABLE_POSITION_RESULT) //
                    .setBody(bodyResp).build();
            Packet packet = Packet.newBuilder()//
                    .setType(PacketType.CMDEXTEND)//
                    .setBody(cmdExtend.toByteString()).build();

            NettyUtils.write(ctx.getChannel(), packet.toByteArray(), null);
        } catch (Exception e) {
            logger.error("response queryTablePosition result goes wrong. request:{}, result:{}, \n{}", cmdBody, bodyResp, ExceptionUtils.getFullStackTrace(e));
        }
    }

    // ================== help method ===================

    private CanalDBSwitchResult doCanalDbSwitch(String cmdBody, ChannelHandlerContext ctx) {
        CanalDBSwitchResult result = new CanalDBSwitchResult();
        CanalDBSwitchRequestData data = CanalDBSwitchRequestData.fromJson(cmdBody, CanalDBSwitchRequestData.class);

        // 请求信息不完整
        String checkError = checkCanalDBSwitchRequestData(data);
        if (StringUtils.isNotBlank(checkError)) {
            String resultMsg = "canal db switch failed: " + checkError;
            logger.error(resultMsg + "; request:{}", data.toJson());
            result.setResultCode(CanalDBSwitchResult.RESULT_CODE_FAILED);
            result.setResultMsg(resultMsg);
            return result;
        }

        // 处理每个实例，确定是否执行切换
        for (CanalInstance instance : embeddedServer.getCanalInstances().values()) {
            AuthenticationInfo runningInfo = ((MysqlEventParser) instance.getEventParser()).getRunningInfo();
            if (addressIsEqual(runningInfo.getAddress(), data.getFromDBData())) { // 当前监听数据库必须和切换的源数据库相同
                CanalDBSwitchInstanceResult instanceResult = null;
                if (addressIsEqual(runningInfo.getAddress(), data.getToDBData())) {
                    instanceResult = new CanalDBSwitchInstanceResult(embeddedServer.getCanalServerWithNetty().getIp(), embeddedServer.getCanalServerWithNetty().getPort(), instance.getDestination());
                    instanceResult.setResultMsg(String.format("the mysql of instance is the same to tagert mysql, request:%s", data.toJson()));
                } else { // 当前监听数据库必须和切换的目标数据库不同
                    instanceResult = doCanalDbSwitch(instance, data); // 执行切换
                }
                result.add(instanceResult); // 添加执行结果
            }
        }

        return result;
    }

    private CanalDBSwitchInstanceResult doCanalDbSwitch(CanalInstance instance, CanalDBSwitchRequestData data) {
        CanalDBSwitchInstanceResult result = new CanalDBSwitchInstanceResult(embeddedServer.getCanalServerWithNetty().getIp(), embeddedServer.getCanalServerWithNetty().getPort(), instance.getDestination());

        try {
            MysqlEventParser mysqlEventParser = (MysqlEventParser) instance.getEventParser();
            AuthenticationInfo newRunningInfo = buildNewAuthenticationInfo(mysqlEventParser.getRunningInfo(), data.getToDBData());
            mysqlEventParser.doSwitch(newRunningInfo);
            logger.info("canal instance db switch successful, destination:{}; from:[{}]; to:[{}]", instance.getDestination(), mysqlEventParser.getRunningInfo().toString(), newRunningInfo.toString());
        } catch (Exception e) {
            String errorMsg = String.format("canal instance db switch goes wrong, destination:%s, request:%s", instance.getDestination(), data.toJson());
            logger.error(errorMsg, ExceptionUtils.getFullStackTrace(e));
            result.setResultCode(CanalDBSwitchResult.RESULT_CODE_FAILED);
            result.setResultMsg(errorMsg + e.getMessage());
        }

        return result;
    }

    private boolean addressIsEqual(InetSocketAddress address, DBData dbData) {
        return address.getHostName().equals(dbData.getHost()) && address.getPort() == dbData.getPort();
    }

    private String checkCanalDBSwitchRequestData(CanalDBSwitchRequestData data) {

        // 切换的源数据库信息是否完整
        String msg = "the source db %s should not be blank.";
        DBData fromDBData = data.getFromDBData();
        if (fromDBData == null) {
            return String.format(msg, "data");
        }
        if (StringUtils.isBlank(fromDBData.getHost())) {
            return String.format(msg, "host");
        }
        if (fromDBData.getPort() == null) {
            return String.format(msg, "port");
        }

        // 切换的源数据库信息是否完整
        msg = "the target db {} should not be blank.";
        DBData toDBData = data.getToDBData();
        if (toDBData == null) {
            return String.format(msg, "data");
        }
        if (StringUtils.isBlank(toDBData.getHost())) {
            return String.format(msg, "host");
        }
        if (toDBData.getPort() == null) {
            return String.format(msg, "port");
        }

        return null;
    }

    /**
     * 生成新的AuthenticationInfo
     * 
     * @param oldRunningInfo
     *            老的AuthenticationInfo
     * @param toDBData
     *            切换的目标数据库，用该数据库信息替换AuthenticationInfo的相应值
     * @return
     */
    private AuthenticationInfo buildNewAuthenticationInfo(AuthenticationInfo oldRunningInfo, DBData toDBData) {
        AuthenticationInfo newRunningInfo = new AuthenticationInfo();

        // 从老的拷贝默认值
        newRunningInfo.setUsername(oldRunningInfo.getUsername());
        newRunningInfo.setPassword(oldRunningInfo.getPassword());
        newRunningInfo.setDefaultDatabaseName(oldRunningInfo.getDefaultDatabaseName());

        // 设置新值
        newRunningInfo.setAddress(new InetSocketAddress(toDBData.getHost(), toDBData.getPort()));
        if (StringUtils.isNotBlank(toDBData.getUsername())) {
            newRunningInfo.setUsername(toDBData.getUsername());
        }
        if (StringUtils.isNotBlank(toDBData.getPassword())) {
            newRunningInfo.setPassword(toDBData.getPassword());
        }

        return newRunningInfo;
    }

    private boolean existTableInMysql(MysqlConnection connection, String queryTableName) throws IOException {
        String sql = "select table_schema, table_name from information.tables where %s limit 1";
        String condition = null;

        // condition
        if (StringUtils.contains(queryTableName, ".")) {
            condition = String.format("table_schema='%s' and table_name='%s'", StringUtils.substringBefore(queryTableName, "."), StringUtils.substringAfter(queryTableName, "."));
        } else {
            condition = String.format("table_name='%s'", queryTableName);
        }

        // query
        ResultSetPacket packet = connection.query(String.format(sql, condition));
        return !CollectionUtils.isEmpty(packet.getFieldValues());
    }

}
