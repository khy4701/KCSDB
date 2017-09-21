package com.kt.restful.scheduler;

import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandConnector;
import com.kt.net.OverloadConnector;
import com.kt.net.StatisticsConnector;
import com.kt.net.StatisticsManager;

public class ClientTask extends TimerTask {

	private static Logger logger = LogManager.getLogger(ClientTask.class);
	
    @Override
    public void run() {
    	CommandConnector.getInstance();
    	OverloadConnector.getInstance();
    	StatisticsConnector.getInstance();
    	StatisticsManager.getInstance().sendStatitics();

    }
}
