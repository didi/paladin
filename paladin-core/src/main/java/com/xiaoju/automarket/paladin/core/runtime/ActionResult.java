package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Optional;

/**
 * @Author Luogh
 * @Date 2020/12/11
 **/
@Getter
@AllArgsConstructor
public class ActionResult {
    private final boolean isSuccess;
    private final Event event;
    private final Duration fireDuration;
}
