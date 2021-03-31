package com.github.lixiang2114.flow.plugins.face;

import com.github.lixiang2114.flow.plugins.CheckPointable;
import com.github.lixiang2114.flow.plugins.ETLFlow;

/**
 * @author Lixiang
 * @description 收集器(Source)插件接口
 * @param <T>
 * @param <D>
 */
public interface SourcePlugin<F> extends CheckPointable,ETLFlow<F>{}
