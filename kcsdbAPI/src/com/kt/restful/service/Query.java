package com.kt.restful.service;

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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;
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
	
//	private static final String GET_QUERY_INFO = "SELECT * FROM ( "
//			+ "SELECT ROWNUM AS RN , A.* FROM ( "
//			+ "SELECT CCM_REFERID, CCM_TIME, CGMDN, CCS_TIME, CCM_REASON, CCM_STATUS, SMS_TIME, SMS_STATUS "
//			+ "FROM CCS_MASTER B "
//			+ "WHERE %s CDMDN = ? AND CCM_TIME >= ? "
//			+ "AND CCS_TIME >= TO_CHAR(SYSDATE-90,'YYYYMMDDHH24MISS') AND CCS_TIME >= ? AND CCS_TIME < ? "
//			+ "AND delete_flag = 'N' ORDER BY CCS_TIME ASC ) A "
//			+ "ORDER BY RN DESC ) WHERE RN BETWEEN  ? AND ?";

//	private static final String COUNT_QUERY = "SELECT COUNT(*) AS CT FROM CCS_MASTER B "
//			+ "WHERE %s CDMDN = ? AND CCM_TIME >= ? "
//			+ "AND CCS_TIME >= TO_CHAR(SYSDATE-90,'YYYYMMDDHH24MISS') AND CCS_TIME >= ? AND CCS_TIME < ? "
//			+ "AND delete_flag = 'N'";



	private static final String GET_QUERY_INFO = "SELECT * FROM ( "
													+ "SELECT ROWNUM AS RN , A.* FROM ( "
													+ "SELECT CCM_REFERID, CCM_TIME, CGMDN, CCS_TIME, CCM_REASON, CCM_STATUS, SMS_TIME, SMS_STATUS "
													+ "FROM CCS_MASTER B "
													+ "WHERE %s CDMDN = ? AND CCM_TIME >= ? "
													+ "AND CCS_TIME >= ? AND CCS_TIME < ? "
													+ "AND delete_flag = 'N' ORDER BY CCS_TIME ASC ) A "
													+ "ORDER BY RN DESC ) WHERE RN BETWEEN  ? AND ?";
	
	private static final String COUNT_QUERY = "SELECT COUNT(*) AS CT FROM CCS_MASTER B "
													+ "WHERE %s CDMDN = ? AND CCM_TIME >= ? "
													+ "AND CCS_TIME >= ? AND CCS_TIME < ? "
													+ "AND delete_flag = 'N'";
	
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
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonbody);
		}		
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonbody);			
		}
		
		String cgmdn = null;
		
		try{
			cgmdn = jsonObj.get("CGMDN").toString();
		}catch(Exception e){
			cgmdn = null;
		}
		
		String cdmdn = jsonObj.get("CDMDN").toString();
		String ccmTime = jsonObj.get("CCM_TIME").toString();
		
		int startRow = Integer.parseInt(jsonObj.get("START_ROW").toString());
		int endRow = Integer.parseInt(jsonObj.get("END_ROW").toString());

		long time = System.currentTimeMillis();
		String s_time = dayTime.format(new Date(time));

		if (CommandManager.getInstance().isLogFlag()) {

			logger.debug("=============================================");
			logger.debug(ApiDefine.QUERY.getName());
			logger.debug("REQUEST URL : " + req.getRequestURL().toString());
			logger.debug("CDMDN : " + cdmdn);
			logger.debug("CCM_TIME : " + ccmTime);
			logger.debug("START_ROW : " + startRow);
			logger.debug("END_ROW : " + endRow);
			logger.debug("CGMDN : " + cgmdn);
			logger.debug("=============================================");
			logger.info(req.getRemoteAddr() + "->KCSDB,"+ cdmdn + "," + ccmTime + "," + startRow +"," + endRow + "," + cgmdn);

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
			PreparedStatement pStatement = null, pStatement2 = null;
			ResultSet rs = null, rs2 = null;
			CCSList ccsList = new CCSList();
			
			mapper = new ObjectMapper();
			
			// PROCESSING 
			String query, cnt_query ;
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
				int index;
				if (cgmdn != null){
					query = String.format(GET_QUERY_INFO, "trim(CGMDN) = ? AND" );
					cnt_query = String.format(COUNT_QUERY, "trim(CGMDN) = ? AND" );	
				}
				else{
					query = String.format(GET_QUERY_INFO, "" );
					cnt_query = String.format(COUNT_QUERY, "" );
				}

				
				pStatement = DBConnector.getInstance().getConnect().prepareStatement(query);
				pStatement2 = DBConnector.getInstance().getConnect().prepareStatement(cnt_query);
				
				if(cgmdn != null){
					index = 1;
					pStatement.setString(index, cgmdn);				
					pStatement2.setString(index, cgmdn);
				}else
					index = 0;				
				
				pStatement.setString(index+1, cdmdn);
				pStatement.setString(index+2, ccmTime);
				pStatement.setString(index+3, startDate);
				pStatement.setString(index+4, curDate);
				pStatement.setInt(index+5, startRow);
				pStatement.setInt(index+6, endRow);
				
				
				pStatement2.setString(index+1, cdmdn);
				pStatement2.setString(index+2, ccmTime);
				pStatement2.setString(index+3, startDate);
				pStatement2.setString(index+4, curDate);

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
				
				rs2 = pStatement2.executeQuery();
				rs2.next();
				int total_count = rs2.getInt("CT");
				if (total_count == 0){
					rspCode = 400;
					logger.error("total_count[0]" + req.getRemoteAddr() + "->KCSDB,"+ cdmdn + "," + ccmTime + "," + startRow +"," + endRow + "," + cgmdn);
				}
				
				ccsList.setTOTAL_ROW(total_count);

				if (rspCode == 400) {
					logger.error("Query Failed.. START DATE[" + startDate + "], END DATE[" + curDate + "]");
				}

			} catch (SQLException e) {
				e.printStackTrace();
				rspCode = 500;
				logger.error("Sql Performance Error: " + e.getMessage());

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
			
			result = gson.toJson(ccsList);
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
