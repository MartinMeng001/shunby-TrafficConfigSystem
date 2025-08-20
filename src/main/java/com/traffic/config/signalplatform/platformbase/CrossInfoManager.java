package com.traffic.config.signalplatform.platformbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.traffic.config.entity.Signal;
import com.traffic.config.service.ConfigService;
import com.traffic.config.service.event.SignalListEvent;
import com.traffic.config.signalplatform.platformbase.entity.CrossInfo;
import com.traffic.config.signalplatform.platformbase.enums.ControlPhase;
import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.events.*;
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
    private WebServiceClient webServiceClient;

    @Autowired
    private ConfigService configService;

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
    @Async("platformEventHandlerThreadPool")
    public void handleSignalListUpdate(SignalListEvent event){
        logger.info("收到 SignalListEvent 事件，开始更新 CrossInfoMap...");
        crossInfoMap.clear(); // 清空现有数据
        List<Signal> signals = event.getRegionList().getRegions();
        for(Signal signal : signals){
            CrossInfo crossInfo = new CrossInfo();
            crossInfo.setCrossid(0);    // 无效项
            // 这里仍沿用原始逻辑，使用 region.getName() 作为 key。
            // 如果实际业务中 sigid 是唯一的标识符，并且后续操作都基于 sigid，
            // 建议这里也使用 sigid 作为 key，以保持一致性。
            crossInfoMap.put(signal.getName(), crossInfo);
            logger.debug("为区域 '{}' 添加了初始 CrossInfo 占位符。", signal.getName());
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

    public boolean guardCrossBySigid(String sigid, int guardMode){
        CrossInfo crossInfo = crossInfoMap.get(sigid);
        if(crossInfo == null) {
            logger.warn("Can't Guard, can't find the cross-sigid: " + sigid);
            return false;
        }
        if(crossInfo.getCrossid()==0) {
            logger.warn("Can't Guard, Invalid crossid: " + crossInfo.getCrossid());
            return false;
        }
        //return 1 == webServiceClient.guardControl(crossInfo.getDevBasicInfo().getIp4G(), guardMode, 0);
        return 1 == webServiceClient.guardControl(crossInfo.getDevBasicInfo().getIp(), guardMode, 0);
    }
    /**
     * Attempts to guard a cross by its signal ID with a retry mechanism.
     *
     * @param sigid The signal ID of the cross.
     * @param guardMode The guard mode to set.
     * @param maxRetries The maximum number of retry attempts.
     * @return true if the guard operation succeeds within the given retries, false otherwise.
     */
    public boolean guardCrossBySigidWithRetry(String sigid, int guardMode, int maxRetries) {
        // --- 参数校验 (Parameter Validation) ---
        CrossInfo crossInfo = crossInfoMap.get(sigid);
        if (crossInfo == null) {
            logger.warn("Can't Guard. Cross not found for sigid: {}", sigid);
            return false;
        }
        if (crossInfo.getCrossid() == 0) {
            logger.warn("Can't Guard. Invalid crossid (0) for sigid: {}", sigid);
            return false;
        }

        // --- 重试逻辑 (Retry Logic) ---
        int retryCount = 0;
        while (retryCount <= maxRetries) {
            logger.info("Attempting to guard cross. Sigid: {}, GuardMode: {}, Attempt: {}", sigid, guardMode, retryCount + 1);
            if(filterGuard(guardMode, sigid)) {
                if (guardCrossBySigid(sigid, guardMode)) {
                    logger.info("Successfully guarded cross with sigid: {}", sigid);
                    return true;
                }
            }

            retryCount++;
            if (retryCount <= maxRetries) {
                try {
                    // Optional: Add a small delay between retries to avoid overwhelming the service
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread was interrupted during retry delay.", e);
                    break;
                }
            }
        }

        logger.error("Failed to guard cross with sigid: {} after {} retries.", sigid, maxRetries);
        return false;
    }
    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleCustomControl(CustomControlEvent event){
        int ctrlPhase = getControlPhase(event.getUpSegmentState(), event.getDownSegmentState());
        if(ctrlPhase == -1) return;
        if(filterGuard(ctrlPhase, event.getSigid()))
            guardCrossBySigid(event.getSigid(), ctrlPhase);
    }

    /** A-上行，对应北全放，B-下行，对应南全放
     * A-Green, B-Green 错误状态,必须亮红灯
     * A-Green，B-Red 放行A
     * A-Red，B-Green 放行B
     * A-Red，B-Red 放行全红
     * A-NULL，B notNull 放行B
     * A notNull，B-NULL 放行A
     * A-NULL，B-NULL 错误状态不放行 亮红灯
     * 路段实际只能控制路段的进入，而不控制路段的离开，不能出现任何一种组合没有灯态返回
     */
    protected int getControlPhase(SegmentState segmentStateA, SegmentState segmentStateB){
        if(segmentStateA==null && segmentStateB==null) return ControlPhase.ALL_RED.getValue();
        if(segmentStateA==null) {
            if(segmentStateB.isYellowFlashState())return ControlPhase.YELLOW_FLASH.getValue();
            if(segmentStateB.isAllRedState())return ControlPhase.ALL_RED.getValue();
            if(segmentStateB.isDownstreamState())return ControlPhase.SOUTH_FULL_GREEN.getValue();
            if(segmentStateB.isUpstreamState())return ControlPhase.NORTH_FULL_GREEN.getValue();
            return ControlPhase.ALL_RED.getValue();
        }
        if(segmentStateB==null) {
            if(segmentStateA.isYellowFlashState())return ControlPhase.YELLOW_FLASH.getValue();
            if(segmentStateA.isAllRedState())return ControlPhase.ALL_RED.getValue();
            if(segmentStateA.isUpstreamState())return ControlPhase.NORTH_FULL_GREEN.getValue();
            if(segmentStateA.isDownstreamState())return ControlPhase.SOUTH_FULL_GREEN.getValue();
            return ControlPhase.ALL_RED.getValue();
        }
        if(segmentStateA.isUpstreamState() && segmentStateB.isDownstreamState()){ ControlPhase.ALL_RED.getValue(); }
        if(segmentStateA.isYellowFlashState() || segmentStateB.isYellowFlashState()){ return ControlPhase.YELLOW_FLASH.getValue(); }
        if(segmentStateB.isUpstreamState()) return ControlPhase.NORTH_FULL_GREEN.getValue();
        if(segmentStateA.isDownstreamState()) return ControlPhase.SOUTH_FULL_GREEN.getValue();

        return ControlPhase.ALL_RED.getValue();
    }
    protected boolean filterGuard(int ctrlPhase, String sigid){
        if(crossInfoMap.containsKey(sigid)) {
            CrossInfo crossInfo = crossInfoMap.get(sigid);
            if(crossInfo.getCtrlPhase()==ctrlPhase) {
                int retryCount = crossInfo.getRetryNums();
                if(retryCount>0){
                    crossInfo.setRetryNums(retryCount-1);
                    return true;
                }
                return false;
            }else {
                crossInfo.setCtrlPhase(ctrlPhase);
                crossInfo.setRetryNums(10);
                return true;
            }
        }
        return false;
    }

    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleAllRedCtrl(AllRedCtrlEvent event){
        if(controlAllCrossesToAllRed(event.getVariables().getMaxRetryNumsAllCtrl())){
            event.getVariables().setAllSignalRed(true);
        }
    }

    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleAllRedClearCtrl(AllClearCtrlEvent event){
        if(controlAllCrossesToAll(event.getVariables().getMaxRetryNumsAllCtrl(), ControlPhase.CANCEL_GUARD.getValue())){
            event.getVariables().setAllSignalRed(false);
        }
    }

    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleGreenCtrl(GreenCtrlEvent event){
//        int segmentId = event.getVariables().getSegmentId();
//        Optional<Segment> segment = configService.getSegmentBySegmentId(segmentId);
//        if(segment.isEmpty()) return;
//        Segment seg = segment.get();
//        int ctrlUp = seg.getUpctrl();
//        int ctrlDown = seg.getDownctrl();
//        String upsigid = seg.getUpsigid();
//        String downsigid = seg.getDownsigid();
//        switch(event.getCurrentState()){
//            case UPSTREAM_GREEN -> {
//                // 上行变绿
//                guardCrossBySigid(upsigid, ctrlUp);
//                // 下行变红
//                guardCrossBySigid(downsigid, ControlPhase.ALL_RED.getValue());
//            }
//            case DOWNSTREAM_GREEN -> {
//                // 上行变红
//                guardCrossBySigid(upsigid, ControlPhase.ALL_RED.getValue());
//                // 下行变绿
//                guardCrossBySigid(downsigid, ctrlDown);
//            }
//        }
    }
    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleRedCtrl(RedCtrlEvent event){
//        int segmentId = event.getVariables().getSegmentId();
//        Optional<Segment> segment = configService.getSegmentBySegmentId(segmentId);
//        if(segment.isEmpty()) return;
//        Segment seg = segment.get();
//        int ctrlUp = seg.getUpctrl();
//        int ctrlDown = seg.getDownctrl();
//        String upsigid = seg.getUpsigid();
//        String downsigid = seg.getDownsigid();
//        guardCrossBySigid(upsigid, ControlPhase.ALL_RED.getValue());
//        guardCrossBySigid(downsigid, ControlPhase.ALL_RED.getValue());
    }

    /**
     * 遍历 crossInfoMap，将所有路口设置为全红模式。
     *
     * @param maxRetries 每个路口控制失败时的最大重试次数。
     */
    public boolean controlAllCrossesToAllRed(int maxRetries) {
        logger.info("开始遍历 crossInfoMap，对所有路口进行全红控制...");

        // 使用一个副本进行遍历，以避免在遍历过程中对原始 Map 进行修改
        Map<String, CrossInfo> crossInfoMapCopy = new HashMap<>(crossInfoMap);

        if (crossInfoMapCopy.isEmpty()) {
            logger.warn("crossInfoMap 为空，没有路口需要控制。");
            return false;
        }

        int allRedCount = 0;
        int failedCount = 0;

        for (Map.Entry<String, CrossInfo> entry : crossInfoMapCopy.entrySet()) {
            String sigid = entry.getKey();
            // 调用已经封装好的 guardCrossBySigidWithRetry 方法
            // 模式参数使用 ControlPhase.ALL_RED.getValue()
            if (guardCrossBySigidWithRetry(sigid, ControlPhase.ALL_RED.getValue(), maxRetries)) {
                allRedCount++;
            } else {
                failedCount++;
                logger.error("对路口 {} 进行全红控制失败。", sigid);
            }
        }

        logger.info("全红控制操作完成。成功 {} 个路口，失败 {} 个路口。", allRedCount, failedCount);
        if(failedCount==0) return true;
        return false;
    }
    /**
     * 遍历 crossInfoMap，将所有路口设置为全红模式。
     *
     * @param maxRetries 每个路口控制失败时的最大重试次数。
     */
    public boolean controlAllCrossesToAll(int maxRetries, int mode) {
        logger.info("开始遍历 crossInfoMap，对所有路口进行控制...");

        // 使用一个副本进行遍历，以避免在遍历过程中对原始 Map 进行修改
        Map<String, CrossInfo> crossInfoMapCopy = new HashMap<>(crossInfoMap);

        if (crossInfoMapCopy.isEmpty()) {
            logger.warn("crossInfoMap 为空，没有路口需要控制。");
            return false;
        }

        int allCtrlCount = 0;
        int failedCount = 0;

        for (Map.Entry<String, CrossInfo> entry : crossInfoMapCopy.entrySet()) {
            String sigid = entry.getKey();
            // 调用已经封装好的 guardCrossBySigidWithRetry 方法
            // 模式参数使用 ControlPhase.ALL_RED.getValue()
            if (guardCrossBySigidWithRetry(sigid, mode, maxRetries)) {
                allCtrlCount++;
            } else {
                failedCount++;
                logger.error("对路口 {} 进行控制失败。", sigid);
            }
        }

        logger.info("控制操作完成。成功 {} 个路口，失败 {} 个路口。", allCtrlCount, failedCount);
        if(failedCount==0) return true;
        return false;
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

        // 只检查需要的ID即可，其它不需要处理
        logger.debug("开始解析并更新 CrossInfoMap...");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 直接将 JsonNode 转换为 List<CrossInfo>
            List<CrossInfo> incomingCrossInfoList = objectMapper.readValue(jsonArrayNode.traverse(), new TypeReference<List<CrossInfo>>() {});

            for (CrossInfo crossInfo : incomingCrossInfoList) {
                // 确保 devBasicInfo 不为空，并且 sigid 可用
                if (crossInfo.getDevBasicInfo() != null && crossInfo.getDevBasicInfo().getSigid() > 0) { // 假设 sigid > 0 是有效ID
                    String sigid = String.valueOf(crossInfo.getDevBasicInfo().getSigid()); // 将 int sigid 转换为 String
                    if(crossInfoMap.containsKey(sigid)==false) continue;
                    crossInfo.setCtrlPhase(crossInfoMap.get(sigid).getCtrlPhase());
                    crossInfo.setRetryNums(crossInfoMap.get(sigid).getRetryNums());
                    crossInfoMap.put(sigid, crossInfo); // 更新或添加 CrossInfo
                    updatedCount++;

                    if (crossInfo.getDevBasicInfo().getOnline() == 1) { // 假设 online == 1 表示在线
                        onlineCount++;
                        //logger.info("CrossInfo单元 '{}' (sigid: {}) 状态：在线。", crossInfo.getCrossName(), sigid);
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
            //logger.info("CrossInfoMap 更新完成。总计收到 {} 条数据，成功更新/添加 {} 条。在线: {} 个, 离线: {} 个, 无效/跳过: {} 个。",
            //        incomingCrossInfoList.size(), updatedCount, onlineCount, offlineCount, notFoundCount);

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
    public boolean checkHealthStatus(){
        Map<String, Integer> unitsCounts = getCrossInfoUnitsCounts();
        if (webServiceClientLastConnected && unitsCounts.getOrDefault("offlineCount", 0) == 0 && unitsCounts.getOrDefault("invalidCount", 0) == 0) {
            logger.info("Platform comm HealthCheck: 所有组件均正常运行。");
            return true;
        } else if (webServiceClientLastConnected && unitsCounts.getOrDefault("offlineCount", 0) > 0) {
            logger.warn("Platform comm HealthCheck: Web服务连接正常，但存在离线或无效的CrossInfo单元。");
            return false;
        } else {
            logger.warn("Platform comm HealthCheck: Web服务连接Down，Web服务连接异常或数据未同步。");
            return false;
        }
    }
}