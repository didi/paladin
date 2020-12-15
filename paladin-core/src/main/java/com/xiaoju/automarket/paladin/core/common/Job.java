package com.xiaoju.automarket.paladin.core.common;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public class Job {
    private String jobId;
    private StatusEnum status;

    public Job(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }
}
