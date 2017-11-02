package com.kt.restful.service;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.kt.net.CommandManager;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.CCSDefine;
import com.kt.restful.model.CCSList;
import com.kt.restful.model.CCSList.CCSInfo;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

@Path("/query")
public class Query {

	private static final String GET_QUERY_INFO = "SELECT CCM_REFERID, CCM_TIME, CGMDN, CCS_TIME, CCM_STATUS, SMS_TIME, SMS_STATUS, CCM_REASON FROM CCS_MASTER ";
	
	private static Logger logger = LogManager.getLogger(Query.class);
	private int rspCode = -1;
	private ObjectMapper mapper = null;
	private SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
	
	private static Calendar c = Calendar.getInstance();

	@POST
	@Produces("application/json;charset=UTF-8")
	// @Consumes("application/x-www-form-urlencoded")
	public Response getData(@Context HttpServletRequest req, @JsonProperty("") String jsonbody) {

		ApiDefine api = ApiDefine.QUERY;
		Gson gson = new Gson();
		String result = null;
		int ret;

		// 01. Read Json Parameter
		JSONObject jsonObj = new JSONObject(jsonbody);

		String cdmdn = jsonObj.get("CDMDN").toString();
		String ccmTime = jsonObj.get("CCM_TIME").toString();

		long time = System.currentTimeMillis();
		String s_time = dayTime.format(new Date(time));

		if (CommandManager.getInstance().isLogFlag()) {

			logger.debug("=============================================");
			logger.debug(ApiDefine.QUERY.getName());
			logger.debug("REQUEST URL : " + req.getRequestURL().toString());
			logger.debug("CDMDN : " + cdmdn);
			logger.debug("CCM_TIME : " + ccmTime);
			logger.debug("=============================================");
			logger.info(req.getRemoteAddr() + "->KCSDB," + cdmdn + "," + ccmTime);

		}

		// 02. Check Allow IP
		ret = ServiceManager.getInstance().checkAllowIP(req, api);
		if (ret < 0) {
			logger.error("Request Remote IP(" + req.getRemoteAddr() + ") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		// Check와 Send를 관리하는 부분은 synchronize로 막기.!
		synchronized (ServiceManager.getInstance()) {

			// 03. Check OverLoad TPS
			ret = ServiceManager.getInstance().checkOverloadTPS(req, api);
			if (ret < 0) {
				return Response.status(503).entity("").build();
			}

			// 04. Statistic Initial
			ServiceManager.getInstance().statInit(req, api);

			// 05. Data Processing
			String sql = null;
			PreparedStatement pStatement = null;
			ResultSet rs = null;
			CCSList ccsList = new CCSList();
			
			mapper = new ObjectMapper();
			
			String query ;
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;

			String startDate = new String(ccmTime.substring(0, 6) + "000000");

			month += 1;
			if (month == 13) {
				year += 1;
				month = 1;
			}

			String curDate = new String(String.format("%d%02d000000", year, month));

			rspCode = 400;
			try {
				sql = GET_QUERY_INFO
						+ "WHERE CDMDN = ? AND CCM_TIME >= ? AND CCS_TIME >= TO_CHAR(current_date-90, 'YYYYMMDD') AND DELETE_FLAG = 'N' AND ccs_time >= ? and ccs_time < ? ORDER BY CCS_TIME DESC LIMIT 100";

				query = String.format(sql, startDate, curDate);

				pStatement = DBConnector.getInstance().getConnect().prepareStatement(sql);
				pStatement.setString(1, cdmdn);
				pStatement.setString(2, ccmTime);
				pStatement.setString(3, startDate);
				pStatement.setString(4, curDate);

				rs = pStatement.executeQuery();
				logger.debug(sql);

				while (rs.next()) {

					CCSInfo ccsInfo = new CCSInfo();
					for (CCSDefine userInfo : CCSDefine.values()) {
						ccsInfo.setData(userInfo.toString(), rs.getString(userInfo.toString()));
					}

					ccsList.addCCSInfo(ccsInfo);
					rspCode = 200;
					logger.debug("Query Success");
				}

				if (rspCode == 400) {
					logger.debug("Query Failed.. START DATE[" + startDate + "], END DATE[" + curDate + "]");
				}

			} catch (SQLException e) {
				e.printStackTrace();
				rspCode = 500;
				logger.error(e.getMessage());

			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());

			} finally {
				try {
					rs.close();
					pStatement.close();
				} catch (SQLException e) { 
					e.printStackTrace();
					logger.error(e.getMessage());
				} finally {
					rs = null;
					pStatement = null;
				}
			}
			
//			try {
//				result = mapper.defaultPrettyPrintingWriter().writeValueAsString(ccsList.getCcs());
//				
//			} catch (JsonGenerationException e) {
//				// TODO Auto-generated catch block
//				logger.error(e.getMessage());
//				rspCode = 500;
//			} catch (JsonMappingException e) {
//				// TODO Auto-generated catch block
//				rspCode = 500;
//				logger.error(e.getMessage());				
//			} catch (IOException e) {
//				rspCode = 500;
//				// TODO Auto-generated catch block
//				logger.error(e.getMessage());				
//			}

			result = gson.toJson(ccsList.getCcs());
			// 05. Statistic Increase
			ServiceManager.getInstance().statIncease(req, api, rspCode);

		}

		time = System.currentTimeMillis();
		dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		s_time = dayTime.format(new Date(time));

		if (CommandManager.getInstance().isLogFlag()) {
			logger.debug("=============================================");
			logger.debug(ApiDefine.QUERY.getName() + " REPONSE");
			logger.debug("Stauts : " + rspCode);
			logger.debug(result);
			logger.debug("=============================================");

			logger.info(req.getRemoteAddr() + "<-KCSDB," + cdmdn + "," + rspCode);

		}

		// 06. Send Result
		ResponseBuilder builder = new ResponseBuilderImpl();
		builder.entity(result);
		builder.status(rspCode);

		return builder.build();
	}
}
