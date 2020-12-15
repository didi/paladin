package com.xiaoju.automarket.paladin.core.runtime.job;

import com.xiaoju.automarket.paladin.core.common.StatusEnum;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;
import com.xiaoju.automarket.paladin.core.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
@Getter
@Setter
@Accessors(chain = true)
public class JobInstance {
    private final String jobId;
    private final JobGraphDescriptor graphDescriptor;
    private StatusEnum jobStatus;

    public JobInstance(JobGraphDescriptor graphDescriptor) {
        this.jobId = Util.generateUUID();
        this.graphDescriptor = graphDescriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobInstance that = (JobInstance) o;
        return jobId.equals(that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
}
