package com.github.lixiang2114.flow.plugins;

import com.github.lixiang2114.flow.plugins.face.Procedure;

/**
 * @author Lixiang
 * @description ETL流程接口
 */
public interface ETLFlow<F> extends Configurable,Procedure<F>{}