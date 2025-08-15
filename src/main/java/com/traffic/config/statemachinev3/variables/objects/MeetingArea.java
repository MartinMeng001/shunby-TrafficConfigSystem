package com.traffic.config.statemachinev3.variables.objects;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MeetingArea {

    private final Set<String> licensePlates;
    private final AtomicInteger vehicleCount = new AtomicInteger(0);;
    private final int maxCapacity;

    public MeetingArea(int maxCapacity) {
        this.licensePlates = new HashSet<>();
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity(){
        return maxCapacity;
    }
    /**
     * 处理车辆进入会车区的事件
     * @param licensePlate 车辆车牌，可以为null
     */
    public void vehicleEntered(String licensePlate) {
        if (licensePlate != null && !licensePlate.isEmpty()) {
            if(this.licensePlates.contains(licensePlate)) return;   // 会车区已经有该车，不需要重复加入
            this.licensePlates.add(licensePlate);
        }
        vehicleCount.incrementAndGet();
        System.out.println("车辆进入会车区，当前车辆计数：" + this.vehicleCount);
        System.out.println("当前车牌集合：" + this.licensePlates);
    }

    /**
     * 处理车辆离开会车区的事件
     * @param licensePlate 车辆车牌，可以为null
     */
    public void vehicleExited(String licensePlate) {
        if (this.vehicleCount.get() > 0) {
            if (licensePlate != null && !licensePlate.isEmpty()) {
                if(!this.licensePlates.contains(licensePlate)) return;  // 会车区已经没有该车，不需要重复删除
                this.licensePlates.remove(licensePlate);
            }
            this.vehicleCount.decrementAndGet();
            System.out.println("车辆离开会车区，当前车辆计数：" + this.vehicleCount);
            System.out.println("当前车牌集合：" + this.licensePlates);
        } else {
            System.out.println("会车区已无车辆，无法处理车辆离开事件。");
        }
    }

    public int getCount(){
        if(licensePlates.size() == vehicleCount.get()) return vehicleCount.get();
        if(licensePlates.size() > vehicleCount.get()) return licensePlates.size();
        return vehicleCount.get();
    }

    public void clear(){
        this.vehicleCount.set(0);
        this.licensePlates.clear();
    }

    public boolean isEmpty(){
        if(licensePlates.isEmpty() && vehicleCount.get() == 0) return true;
        return false;
    }
    public boolean canAcceptVehicle(){
        return getCount() < maxCapacity;
    }

    // 可以添加 getter 方法来获取内部状态
    public int getVehicleCount() {
        return vehicleCount.get();
    }

    public Set<String> getLicensePlates() {
        return licensePlates;
    }
}
