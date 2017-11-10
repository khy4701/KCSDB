package com.kt.restful.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kt.net.CommandManager;
import com.kt.restful.model.ApiDefine;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

@Path("/updateDeleteFlag")
@Produces("application/json;charset=UTF-8")

public class UpdateDelete {

	private static String UPT_DELETE_FLAG_SQL = "UPDATE %s SET DELETE_FLAG = ? WHERE CCM_REFERID = ? AND CCM_TIME = ?";

	private static Logger logger = LogManager.getLogger(UpdateDelete.class);

	@PUT
	@Produces("application/json;charset=UTF-8")
	public Response updateDelete(@Context HttpServletRequest req, @JsonProperty("") String jsonbody) {

		ApiDefine api = ApiDefine.UPDATE;
		JSONArray list = null;
		int ret, emptyDelFlag = 0;

		// 01. Read Json Parameter
		JSONObject jsonObj = new JSONObject(jsonbody);

		String ccmReferId = jsonObj.get("CCM_REFERID").toString();
		String ccmTime = jsonObj.get("CCM_TIME").toString();
		String deleteFlag;

		try {
			deleteFlag = jsonObj.get("DELETE_FLAG").toString();
		} catch (Exception e) {
			deleteFlag = "Y";
			emptyDelFlag = 1;
		}

		long time = System.currentTimeMillis();
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String s_time = dayTime.format(new Date(time));

		if (CommandManager.getInstance().isLogFlag()) {
		
			logger.debug("=============================================");
			logger.debug(ApiDefine.UPDATE.getName());
			logger.debug("REQUEST URL : " + req.getRequestURL().toString());
			logger.debug("CCM_REFERID : " + ccmReferId);
			logger.debug("CCM_TIME : " + ccmTime);
			if (emptyDelFlag == 1)
				logger.debug("DELETE_FLAG : " + deleteFlag);
			logger.debug("=============================================");
			logger.info(req.getRemoteAddr() + "->KCSDB," + ccmReferId + "," + ccmTime
					+ "," + deleteFlag);

		}

		// 02. Check Allow IP
		ret = ServiceManager.getInstance().checkAllowIP(req, api);
		if (ret < 0) {
			logger.error("Request Remote IP(" + req.getRemoteAddr() + ") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		synchronized (ServiceManager.getInstance()) {

			// 03. Check OverLoad TPS
			ret = ServiceManager.getInstance().checkOverloadTPS(req, api);
			if (ret < 0) {
				return Response.status(503).entity("").build();
			}

			// 04. Statistic Initial
			ServiceManager.getInstance().statInit(req, api);

			// 05. Data Processing
			JSONObject responseJSONObject = new JSONObject();
			PreparedStatement pStatement = null;
			int result = 0;

			String query = null;
			String year = ccmTime.substring(0, 4);
			String month = ccmTime.substring(4, 6);

			query = String.format(UPT_DELETE_FLAG_SQL, "ccs_master_p_" + year + month);

			try {
				pStatement = DBConnector.getInstance().getConnect().prepareStatement(query);
				pStatement.setString(1, deleteFlag);
				pStatement.setString(2, ccmReferId);
				pStatement.setString(3, ccmTime);

				result = pStatement.executeUpdate();
				logger.debug(UPT_DELETE_FLAG_SQL);

				if (result == 1) {
					rspCode = 200;
					list = new JSONArray();

					responseJSONObject.put("CCM_REFERID", ccmReferId);
					responseJSONObject.put("CCM_TIME", ccmTime);
					responseJSONObject.put("DELETE_FLAG", deleteFlag);
					list.put(responseJSONObject);
				} else {
					rspCode = 400;
					logger.debug("Query Failed.. ");
				}

			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage());

			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Update Error :" + e.getMessage());

			} finally {
				try {
					pStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					rspCode = 500;
				} finally {
					pStatement = null;
				}
			}

			// 06. Statistic Increase
			ServiceManager.getInstance().statIncease(req, api, rspCode);
		}

		time = System.currentTimeMillis();
		dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		s_time = dayTime.format(new Date(time));

		if (CommandManager.getInstance().isLogFlag()) {

			logger.debug("=============================================");
			logger.debug(ApiDefine.UPDATE.getName() + " REPONSE");
			logger.debug("Stauts : " + rspCode);
			if (list != null)
				logger.debug(list.toString());
			logger.debug("=============================================");

			logger.info(req.getRemoteAddr() + "<-KCSDB," + ccmReferId + "," + rspCode);

		}

		// 07. Send Response Result
		ResponseBuilder builder = new ResponseBuilderImpl();
		if (list != null) {
			builder.entity(list.toString());
		}

		builder.status(rspCode);

		return builder.build();
	}

	private int rspCode = -1;

}