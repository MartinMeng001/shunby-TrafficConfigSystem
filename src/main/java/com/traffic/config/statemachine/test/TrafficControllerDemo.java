package com.traffic.config.statemachine.test;

import com.traffic.config.statemachine.items.TrafficController;

public class TrafficControllerDemo {
    public static void main(String[] args) throws InterruptedException {
        TrafficController controller = new TrafficController();

        System.out.println("交通信号控制系统原型启动");
        controller.printStatus();

        // 模拟运行
        for (int i = 0; i < 20; i++) {
            Thread.sleep(2000); // 等待2秒

            // 执行控制循环
            controller.controlLoop();

            // 随机生成车辆事件
            if (Math.random() < 0.6) { // 60%概率有车辆事件
                String segmentId = Math.random() < 0.5 ? "segment1" : "segment2";
                String vehicleId = "V" + (int)(Math.random() * 1000);
                boolean isEntering = Math.random() < 0.7; // 70%概率是进入

                controller.simulateVehicleEvent(segmentId, vehicleId, isEntering);
            }

            controller.printStatus();
        }
    }
}
