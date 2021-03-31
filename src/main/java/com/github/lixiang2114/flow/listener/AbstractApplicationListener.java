package com.github.lixiang2114.flow.listener;

import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;

/**
 * @author Lixiang
 * @description 抽象应用监听器
 */
public abstract class AbstractApplicationListener implements SpringApplicationRunListener,Ordered{

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
