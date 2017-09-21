package com.kt.restful.service;

import javax.servlet.http.HttpServletRequest;

import com.kt.net.OverloadManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.AllowIpProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.StatisticsModel;

public class ServiceManager {

	private static ServiceManager serviceManager;

	public static ServiceManager getInstance() {
		if (serviceManager == null)
			serviceManager = new ServiceManager();

		return serviceManager;
	}

	@SuppressWarnings("static-access")
	public int checkOverloadTPS(Object reqObj, ApiDefine api) {
		// TODO Auto-generated method stub
		HttpServletRequest req = (HttpServletRequest) reqObj;

		if (StatisticsManager.getInstance().getTps() >= OverloadManager.getInstance().getOverloadTps()) {
			// System.out.println("Test - overTps stat - " +
			// StatisticsManager.getInstance().getTps() + " over - " +
			// OverloadManager.getInstance().getOverloadTps());
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if (StatisticsManager.getInstance().getStatisticsHash()
						.containsKey(req.getRemoteAddr() + api.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
							.plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
							.plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
							.plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr() + api.getName(),
							new StatisticsModel(api.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
							.plusError503();
				}
			}
			return -1;
		}
		return 0;
	}

	@SuppressWarnings("static-access")
	public int checkAllowIP(Object reqObj, ApiDefine api) {

		HttpServletRequest req = (HttpServletRequest) reqObj;

		for (String ipInfo : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {

			String[] allowIp = ipInfo.split(":");

			if (allowIp[0].equals(req.getRemoteAddr()))
				return 1;
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if (StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr() + api.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
						.plusTotal();
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName()).plusFail();
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
						.plusError403();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr() + api.getName(),
						new StatisticsModel(api.getName(), req.getRemoteAddr(), 1, 0, 1));
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
						.plusError403();
			}
		}
		return -1;
	}

	@SuppressWarnings("static-access")
	public void statInit(Object reqObj, ApiDefine api) {

		HttpServletRequest req = (HttpServletRequest) reqObj;

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if (StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr() + api.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
						.plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr() + api.getName(),
						new StatisticsModel(api.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}
	}

	@SuppressWarnings("static-access")
	public void statIncease(Object reqObj, ApiDefine api, int rspCode) {

		HttpServletRequest req = (HttpServletRequest) reqObj;

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if (rspCode == 200) {
				if (StatisticsManager.getInstance().getStatisticsHash()
						.containsKey(req.getRemoteAddr() + api.getName())) {
					// StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+api.getName()).plusSucc();
					if (StatisticsManager.getInstance().getStatisticsHash()
							.get(req.getRemoteAddr() + api.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr() + api.getName(),
								new StatisticsModel(api.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr() + api.getName(),
							new StatisticsModel(api.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if (StatisticsManager.getInstance().getStatisticsHash()
						.containsKey(req.getRemoteAddr() + api.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
							.plusFail();

					if (rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr() + api.getName(),
							new StatisticsModel(api.getName(), req.getRemoteAddr(), 0, 0, 1));

					if (rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr() + api.getName())
								.plusError501();
				}
			}
		}
	}
}
