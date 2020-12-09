package com.xiaoju.automarket.paladin.core.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
@Data
@Accessors(chain = true)
public class Job {
    private String jobId;
    private JobStatus status;

    public Job(String jobId) {
        this.jobId = jobId;
    }
}
