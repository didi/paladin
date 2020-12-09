package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.common.Event;
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

    private ActionDescriptor<? extends Event, ? extends Event> rootAction;
    private Map<String, DependencyDescriptor<? extends Event>> dependencies;
    private Map<String, List<String>> action2DependencyMap;

}
