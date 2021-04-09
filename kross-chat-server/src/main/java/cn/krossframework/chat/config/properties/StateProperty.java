package cn.krossframework.chat.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.state")
@Component
public class StateProperty {

    private int workerUpdatePeriod;
    private int workerCapacity;
    private int workerThreadSize;
    private int removeEmptyWorkerPeriod;
    private int removeDeposedStateGroupPeriod;
    private int taskDispatcherSize;

    public int getWorkerUpdatePeriod() {
        return workerUpdatePeriod;
    }

    public void setWorkerUpdatePeriod(int workerUpdatePeriod) {
        this.workerUpdatePeriod = workerUpdatePeriod;
    }

    public int getWorkerCapacity() {
        return workerCapacity;
    }

    public void setWorkerCapacity(int workerCapacity) {
        this.workerCapacity = workerCapacity;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public void setWorkerThreadSize(int workerThreadSize) {
        this.workerThreadSize = workerThreadSize;
    }

    public int getRemoveEmptyWorkerPeriod() {
        return removeEmptyWorkerPeriod;
    }

    public void setRemoveEmptyWorkerPeriod(int removeEmptyWorkerPeriod) {
        this.removeEmptyWorkerPeriod = removeEmptyWorkerPeriod;
    }

    public int getRemoveDeposedStateGroupPeriod() {
        return removeDeposedStateGroupPeriod;
    }

    public void setRemoveDeposedStateGroupPeriod(int removeDeposedStateGroupPeriod) {
        this.removeDeposedStateGroupPeriod = removeDeposedStateGroupPeriod;
    }

    public int getTaskDispatcherSize() {
        return taskDispatcherSize;
    }

    public void setTaskDispatcherSize(int taskDispatcherSize) {
        this.taskDispatcherSize = taskDispatcherSize;
    }
}