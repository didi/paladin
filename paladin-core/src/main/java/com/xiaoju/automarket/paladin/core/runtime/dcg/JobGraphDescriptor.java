package com.xiaoju.automarket.paladin.core.runtime.dcg;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
public class JobGraphDescriptor {
    private Map<String, ActionDescriptor> actions;
    private Map<String, DependencyDescriptor> dependencies;
}
