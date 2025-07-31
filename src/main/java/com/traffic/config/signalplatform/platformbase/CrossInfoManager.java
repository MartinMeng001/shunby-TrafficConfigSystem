package com.traffic.config.signalplatform.platformbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.traffic.config.entity.Region;
import com.traffic.config.service.event.SignalListEvent;
import com.traffic.config.signalplatform.platformbase.entity.CrossInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class CrossInfoManager {
    private static final Logger logger = LoggerFactory.getLogger(CrossInfoManager.class);
    // 存储 sigid 到 CrossInfo 对象的映射
    private final Map<String, CrossInfo> crossInfoMap;

    @Autowired // 注入 TaskScheduler
    private TaskScheduler taskScheduler;

    @Autowired
    WebServiceClient webServiceClient;

    // 用于存储 webServiceClient 的最新连接状态
    private volatile boolean webServiceClientLastConnected = false;

    public CrossInfoManager() {
        this.crossInfoMap = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        logger.info("CrossInfoManager 初始化中...");
    }

    @EventListener
    @Async
    public void handleSignalListUpdate(SignalListEvent event){
        logger.info("收到 SignalListEvent 事件，开始更新 CrossInfoMap...");
        crossInfoMap.clear(); // 清空现有数据
        List<Region> regions = event.getRegionList().getRegions();
        for(Region region : regions){
            CrossInfo crossInfo = new CrossInfo();
            crossInfo.setCrossid(0);    // 无效项
            // 这里仍沿用原始逻辑，使用 region.getName() 作为 key。
            // 如果实际业务中 sigid 是唯一的标识符，并且后续操作都基于 sigid，
            // 建议这里也使用 sigid 作为 key，以保持一致性。
            crossInfoMap.put(region.getName(), crossInfo);
            logger.debug("为区域 '{}' 添加了初始 CrossInfo 占位符。", region.getName());
        }

        // 首次立即尝试更新所有 CrossInfo
        logger.info("立即尝试从平台更新所有 CrossInfo 信息。");
        updateAllCrossInfoFromPlatform();

        // 首次延迟 60 秒后开始执行，之后每 60 秒执行一次，进行定时检查
        taskScheduler.scheduleAtFixedRate(() -> {
            logger.info("CrossInfoManager: 定时任务触发，开始检查平台连接和单元状态...");
            boolean currentConnectionStatus = updateAllCrossInfoFromPlatform(); // 检查 webServiceClient 连接
            if (currentConnectionStatus) {
                // 如果 webServiceClient 连接正常，则进一步检查单元状态
                checkCrossInfoUnitsStatus();
            } else {
                logger.warn("由于 webServiceClient 连接异常，本次跳过 CrossInfo 单元状态的详细统计。");
            }
        }, TimeUnit.SECONDS.toMillis(60)); // 首次延迟 60 秒，之后每 60 秒执行一次
        logger.info("CrossInfoManager 定时任务已启动，每 60 秒执行一次平台连接和单元状态检查。");
    }

    public boolean guardCrossAs(String sigid, int guardMode){
        CrossInfo crossInfo = crossInfoMap.get(sigid);
        if(crossInfo == null) {
            logger.warn("Can't Guard, can't find the cross-sigid: " + sigid);
            return false;
        }
        if(crossInfo.getCrossid()==0) {
            logger.warn("Can't Guard, Invalid crossid: " + crossInfo.getCrossid());
            return false;
        }
        return 1 == webServiceClient.guardControl(crossInfo.getDevBasicInfo().getIp4G(), guardMode, 0);
    }

    /**
     * 尝试从平台更新所有 CrossInfo 信息，并更新 webServiceClient 的连接状态。
     * @return 如果成功连接并获取到数据，返回 true；否则返回 false。
     */
    public boolean updateAllCrossInfoFromPlatform(){
        logger.debug("调用 webServiceClient.testConnection() 检查平台连接...");
        JsonNode allcrosses = webServiceClient.testConnection();
        if(allcrosses != null){
            logger.info("webServiceClient 连接正常，并成功获取到 CrossInfo 原始数据。");
            updateCrossInfo(allcrosses); // 更新 crossInfoMap
            webServiceClientLastConnected = true; // 更新连接状态
            return true;
        } else {
            logger.warn("webServiceClient 连接异常或未从平台获取到数据。");
            webServiceClientLastConnected = false; // 更新连接状态
            return false;
        }
    }

    /**
     * 根据传入的 JsonNode 格式的 JSON 数组更新 Map，并返回更新统计信息。
     *
     * @param jsonArrayNode 包含 CrossInfo 对象的 JsonNode 数组
     * @return 包含更新统计信息的 Map，键为 "onlineCount", "offlineCount", "notFoundCount"
     */
    public Map<String, Integer> updateCrossInfo(JsonNode jsonArrayNode) {
        int onlineCount = 0;
        int offlineCount = 0;
        int notFoundCount = 0;
        int updatedCount = 0; // 新增：记录实际更新的条目数

        logger.debug("开始解析并更新 CrossInfoMap...");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 直接将 JsonNode 转换为 List<CrossInfo>
            List<CrossInfo> incomingCrossInfoList = objectMapper.readValue(jsonArrayNode.traverse(), new TypeReference<List<CrossInfo>>() {});

            for (CrossInfo crossInfo : incomingCrossInfoList) {
                // 确保 devBasicInfo 不为空，并且 sigid 可用
                if (crossInfo.getDevBasicInfo() != null && crossInfo.getDevBasicInfo().getSigid() > 0) { // 假设 sigid > 0 是有效ID
                    String sigid = String.valueOf(crossInfo.getDevBasicInfo().getSigid()); // 将 int sigid 转换为 String
                    crossInfoMap.put(sigid, crossInfo); // 更新或添加 CrossInfo
                    updatedCount++;

                    if (crossInfo.getDevBasicInfo().getOnline() == 1) { // 假设 online == 1 表示在线
                        onlineCount++;
                        logger.trace("CrossInfo单元 '{}' (sigid: {}) 状态：在线。", crossInfo.getCrossName(), sigid);
                    } else { // 假设 online == 0 表示离线
                        offlineCount++;
                        logger.warn("CrossInfo单元 '{}' (sigid: {}) 状态：离线！", crossInfo.getCrossName(), sigid);
                        // 可以在此处添加告警逻辑，例如：alertService.sendOfflineAlert(sigid, crossInfo.getCrossName());
                    }
                } else {
                    // 如果 devBasicInfo 为空，或者 sigid 无效，则计为未找到有效信息
                    notFoundCount++;
                    logger.warn("收到无效的 CrossInfo (缺少 devBasicInfo 或 sigid 无效): crossName='{}'",
                            crossInfo.getCrossName() != null ? crossInfo.getCrossName() : "未知 Cross");
                }
            }
            logger.info("CrossInfoMap 更新完成。总计收到 {} 条数据，成功更新/添加 {} 条。在线: {} 个, 离线: {} 个, 无效/跳过: {} 个。",
                    incomingCrossInfoList.size(), updatedCount, onlineCount, offlineCount, notFoundCount);

        } catch (Exception e) {
            logger.error("更新 CrossInfoMap 发生异常: {}", e.getMessage(), e);
            // 在实际应用中，你可能需要更详细的错误处理和日志记录
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("onlineCount", onlineCount);
        result.put("offlineCount", offlineCount);
        result.put("notFoundCount", notFoundCount);
        return result;
    }

    /**
     * 检查并记录当前 crossInfoMap 中所有 CrossInfo 单元的在线状态。
     * 这是一个内部方法，主要用于定时任务中记录日志。
     * 如果需要对外暴露，请使用 getHealthStatus()。
     */
    private void checkCrossInfoUnitsStatus() {
        Map<String, Integer> currentStatus = getCrossInfoUnitsCounts();
        logger.info("当前 CrossInfoMap 状态汇总：在线 {} 个, 离线 {} 个, 无效 {} 个。",
                currentStatus.getOrDefault("onlineCount", 0),
                currentStatus.getOrDefault("offlineCount", 0),
                currentStatus.getOrDefault("invalidCount", 0));
    }

    /**
     * 获取 crossInfoMap 中所有 CrossInfo 单元的在线状态计数。
     * @return 包含在线、离线和无效单元计数的 Map
     */
    private Map<String, Integer> getCrossInfoUnitsCounts() {
        int currentOnlineCount = 0;
        int currentOfflineCount = 0;
        int currentInvalidCount = 0;

        // 遍历 crossInfoMap 的副本以避免并发修改问题
        for (Map.Entry<String, CrossInfo> entry : new HashMap<>(crossInfoMap).entrySet()) {
            CrossInfo crossInfo = entry.getValue();
            if (crossInfo.getDevBasicInfo() != null && crossInfo.getDevBasicInfo().getSigid() > 0) {
                if (crossInfo.getDevBasicInfo().getOnline() == 1) {
                    currentOnlineCount++;
                } else {
                    currentOfflineCount++;
                }
            } else {
                currentInvalidCount++;
            }
        }

        Map<String, Integer> status = new HashMap<>();
        status.put("onlineCount", currentOnlineCount);
        status.put("offlineCount", currentOfflineCount);
        status.put("invalidCount", currentInvalidCount);
        return status;
    }

    /**
     * 获取指定 sigid 的 CrossInfo 对象。
     *
     * @param sigid 目标 sigid
     * @return 对应的 CrossInfo 对象（如果存在），否则返回 null
     */
    public CrossInfo getCrossInfoBySigid(String sigid) {
        return crossInfoMap.get(sigid);
    }

    /**
     * 获取所有 CrossInfo 对象的副本。
     * @return 所有 CrossInfo 对象的 Map 副本。
     */
    public Map<String, CrossInfo> getAllCrossInfo() {
        return new HashMap<>(crossInfoMap); // 返回一个副本，防止外部直接修改内部 Map
    }

    /**
     * **对外健康检查接口**
     * 提供当前 CrossInfoManager 的健康状态，包括 webServiceClient 连接状态
     * 和 crossInfoMap 中各个单元的汇总状态。
     *
     * @return 包含健康状态信息的 Map
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();

        // 1. webServiceClient 连接状态
        healthStatus.put("webServiceClientConnected", webServiceClientLastConnected);
        healthStatus.put("webServiceClientStatusMessage", webServiceClientLastConnected ? "正常连接" : "连接异常或未获取到数据");

        // 2. crossInfoMap 单元状态统计
        Map<String, Integer> unitsCounts = getCrossInfoUnitsCounts();
        healthStatus.put("crossInfoUnitsCounts", unitsCounts);
        healthStatus.put("crossInfoUnitsTotal", crossInfoMap.size()); // 当前 Map 中存储的总单元数

        if (webServiceClientLastConnected && unitsCounts.getOrDefault("offlineCount", 0) == 0 && unitsCounts.getOrDefault("invalidCount", 0) == 0) {
            healthStatus.put("overallStatus", "UP");
            healthStatus.put("overallMessage", "所有组件均正常运行。");
        } else if (webServiceClientLastConnected && unitsCounts.getOrDefault("offlineCount", 0) > 0) {
            healthStatus.put("overallStatus", "DEGRADED");
            healthStatus.put("overallMessage", "Web服务连接正常，但存在离线或无效的CrossInfo单元。");
        } else {
            healthStatus.put("overallStatus", "DOWN");
            healthStatus.put("overallMessage", "Web服务连接异常或数据未同步。");
        }

        logger.debug("健康检查接口被调用，返回状态: {}", healthStatus);
        return healthStatus;
    }
}