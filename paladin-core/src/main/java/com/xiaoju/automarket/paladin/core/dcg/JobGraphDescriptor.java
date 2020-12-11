package com.xiaoju.automarket.paladin.core.dcg;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class JobGraphDescriptor {

    private ActionDescriptor rootAction;
    private Map<Integer, DependencyDescriptor> dependencies;
    private Map<Integer, List<Integer>> action2DependencyMap;

}
