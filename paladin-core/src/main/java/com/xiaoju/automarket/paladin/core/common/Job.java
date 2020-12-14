package com.xiaoju.automarket.paladin.core.common;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public class Job {
    private String jobId;
    private JobStatusEnum status;

    public Job(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobStatusEnum getStatus() {
        return status;
    }

    public void setStatus(JobStatusEnum status) {
        this.status = status;
    }
}
