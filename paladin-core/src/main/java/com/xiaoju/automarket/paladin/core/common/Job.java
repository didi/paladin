package com.xiaoju.automarket.paladin.core.common;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public class Job {
    private String jobId;
    private ExecutionStateEnum status;

    public Job(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public ExecutionStateEnum getStatus() {
        return status;
    }

    public void setStatus(ExecutionStateEnum status) {
        this.status = status;
    }
}
