package com.traffic.config.controller;

import com.traffic.config.entity.GlobalConfig;
import com.traffic.config.entity.Segment;
import com.traffic.config.entity.Segments;
import com.traffic.config.entity.SingleLane;
import com.traffic.config.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 获取完整配置
     */
    @GetMapping("/full")
    public ResponseEntity<SingleLane> getFullConfig() {
        try {
            SingleLane config = configService.loadConfig();

            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取全局配置
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalConfig> getGlobalConfig() {
        try {
            GlobalConfig global = configService.getGlobalConfig();
            return ResponseEntity.ok(global);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新全局配置
     */
    @PutMapping("/global")
    public ResponseEntity<String> updateGlobalConfig(@RequestBody GlobalConfig globalConfig) {
        try {
            configService.updateGlobalConfig(globalConfig.getAllRed(), globalConfig.getMaxAllRed());
            return ResponseEntity.ok("全局配置更新成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有路段配置
     */
    @GetMapping("/segments")
    public ResponseEntity<List<Segment>> getAllSegments() {
        try {
            List<Segment> segments = configService.getAllSegments();
            return ResponseEntity.ok(segments);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据信号灯ID获取路段配置
     */
    @GetMapping("/segments/{sigid}")
    public ResponseEntity<Segment> getSegmentBySigid(@PathVariable String sigid) {
        try {
            Optional<Segment> segment = configService.getSegmentBySigid(sigid);
            return segment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据名称获取路段配置
     */
    @GetMapping("/segments/byname/{name}")
    public ResponseEntity<Segment> getSegmentByName(@PathVariable String name) {
        try {
            Optional<Segment> segment = configService.getSegmentByName(name);
            return segment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建新路段配置
     */
    @PostMapping("/segments")
    public ResponseEntity<String> addSegment(@RequestBody Segment segment) {
        try {
            // 检查信号灯ID是否已存在
            if (configService.getSegmentBySigid(segment.getUpsigid()).isPresent()) {
                return ResponseEntity.badRequest().body("信号灯ID已存在: " + segment.getUpsigid());
            }

            configService.addSegment(segment);
            return ResponseEntity.ok("路段配置添加成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新路段配置
     */
    @PutMapping("/segments/{sigid}")
    public ResponseEntity<String> updateSegment(@PathVariable String sigid, @RequestBody Segment segment) {
        try {
            // 确保请求体中的sigid与路径参数一致
            segment.setUpsigid(sigid);

            boolean updated = configService.updateSegment(sigid, segment);
            if (updated) {
                return ResponseEntity.ok("路段配置更新成功");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除路段配置
     */
    @DeleteMapping("/segments/{sigid}")
    public ResponseEntity<String> deleteSegment(@PathVariable String sigid) {
        try {
            boolean deleted = configService.deleteSegment(sigid);
            if (deleted) {
                return ResponseEntity.ok("路段配置删除成功");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("删除失败: " + e.getMessage());
        }
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshCache() {
        try {
            configService.refreshCache();
            return ResponseEntity.ok("配置缓存刷新成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("刷新失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            configService.loadConfig();
            return ResponseEntity.ok("配置服务正常");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("配置服务异常: " + e.getMessage());
        }
    }
    /**
     * 调试配置加载情况
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugConfig() {
        try {
            SingleLane config = configService.loadConfig();
            Map<String, Object> result = new HashMap<>();

            if (config != null && config.getSegments() != null) {
                Segments segments = config.getSegments();

                result.put("getSize", segments.getSize());
                result.put("getActualSize", segments.getSegmentList().size());
                //result.put("getOriginalSize", segments.getOriginalSize());
                result.put("toString", segments.toString());
                result.put("listIsNull", segments.getSegmentList() == null);

                if (segments.getSegmentList() != null) {
                    result.put("listSize", segments.getSegmentList().size());
                    result.put("firstSegmentName",
                            segments.getSegmentList().isEmpty() ? "no segments" :
                                    segments.getSegmentList().get(0).getName());
                }
            } else {
                result.put("error", "config or segments is null");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("exception", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}