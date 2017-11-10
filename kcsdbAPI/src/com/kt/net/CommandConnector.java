package com.kt.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.kcsdbProperty;
import com.kt.util.Util;

public class CommandConnector extends Connector2 {

	private static Logger logger = LogManager.getLogger(CommandConnector.class);

	private DataInputStream din;

	private boolean msgReadStarted;
	private int reservedMsgSize;
	private int totalReadSize, currentReadSize;
	private int[] msgSize;
	private static CommandConnector dbmConnector;
	
	private int mapType = 0;

	public static int reqId = 1000;

	public static CommandConnector getInstance() {
		if(dbmConnector == null || !dbmConnector.isConnected()) {
			dbmConnector = new CommandConnector();
		}
		return dbmConnector;
	}

	public CommandConnector() {
		super(CommandManager.getInstance(), kcsdbProperty.getPropPath("kcsdb_command_ipaddress"), Integer.parseInt(kcsdbProperty.getPropPath("kcsdb_command_port")));
		msgSize = new int[4];
		for( int i=0; i<msgSize.length; i++ )
			msgSize[i] = -1;
	}

	protected boolean sendMessage() {
		try {
			dataOut.writeInt(0);
			dataOut.flush();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	protected boolean sendMessage(String command, String imsi, String ipAddress, String name, String sendMsg, int jobNo) {
		try {

			int bodyLen = 32 + 16 + 64 + 16 + 4 + sendMsg.length();


			if(CommandManager.getInstance().isLogFlag()) {
				
				logger.info("===============================================");
				logger.info("PROVS -> MMIB");
				logger.info("bodyLen : " + bodyLen);
				logger.info("command : " + command);
				logger.info("imsi : " + imsi);
				logger.info("ipAddress : " + ipAddress);
				logger.info("name : " + name);
				logger.info("jobNo : " + jobNo);
				logger.info("sendMsg ");
				logger.info(sendMsg);
				logger.info("===============================================");
			}

			int mapType = 4;
			if (command.startsWith("TRACE_")) {
				mapType = 17;
				command = command.replace("TRACE_", "");
			}

			dataOut.writeInt(byteToInt(toBytes(bodyLen), ByteOrder.BIG_ENDIAN));
			//			dataOut.writeInt(bodyLen);
			//Statistics Count
			dataOut.writeInt(mapType);
			dataOut.writeInt(0);
			//			for(int i = 0; i < 4; i++)
			//				dataOut.write("0".getBytes());

			//command
			dataOut.write(command.getBytes());
			for(int i = 0; i < 32 - command.length(); i++)
				dataOut.write("\0".getBytes());

			//imsi
			dataOut.write(imsi.getBytes());
			for(int i = 0; i < 16 - imsi.length(); i++)
				dataOut.write("\0".getBytes());

			//ipAddress
			dataOut.write(ipAddress.getBytes());
			for(int i = 0; i < 64 - ipAddress.length(); i++)
				dataOut.write("\0".getBytes());
			
			//name
			dataOut.write(name.getBytes());
			for(int i = 0; i < 16 - name.length(); i++)
				dataOut.write("\0".getBytes());

			dataOut.writeInt(jobNo);

			//sendMsg
			dataOut.write(sendMsg.getBytes());

			dataOut.flush();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return false;
		}

		return true;
	}

	private static byte[] toBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte)(i>>24);
		result[1] = (byte)(i>>16);
		result[2] = (byte)(i>>8);
		result[3] = (byte)(i);
		return result;
	}   

	public static int byteToInt(byte[] bytes, ByteOrder order) {

		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(order);

		// buff사이즈는 4인 상태임
		// bytes를 put하면 position과 limit는 같은 위치가 됨.
		buff.put(bytes);
		// flip()가 실행 되면 position은 0에 위치 하게 됨.
		buff.flip();

		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
	}

	protected void readMessage() throws IOException {
		
		if (msgReadStarted == false) {
			//			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.BIG_ENDIAN);
			//			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.LITTLE_ENDIAN);
			reservedMsgSize = dataIn.readInt();
			//			reservedMsgSize = reservedMsgSize - 4;
			if (reservedMsgSize > BUFFER_SIZE) {
				logger.error(
						"(DBM) ReservedMsgSize is larger than "+ BUFFER_SIZE+ " : " + reservedMsgSize);
				//				throw new IOException("Larger than " + BUFFER_SIZE + " bytes");
				msgReadStarted = false;
				totalReadSize = 0;

				int avail = dataIn.available();
				dataIn.skipBytes(avail);
				return;
			}

			mapType = dataIn.readInt();
			dataIn.skipBytes(4);
//			dataIn.skipBytes(8);

			msgReadStarted = true;
			totalReadSize = 0;
		}

		currentReadSize = dataIn.read(buffer, totalReadSize, reservedMsgSize - totalReadSize);
		if (totalReadSize + currentReadSize == reservedMsgSize) {

			din = new DataInputStream(new ByteArrayInputStream(buffer));
			try {

				if(mapType == 3) {				
					byte[] commandBuffer = new byte[32];
					byte[] imsiBuffer = new byte[16];
					byte[] ipAddressBuffer = new byte[64];
					byte[] nameBuffer = new byte[16];

					din.read(commandBuffer, 0, commandBuffer.length);
					String command = Util.nullTrim(new String(commandBuffer));

					din.read(imsiBuffer, 0, imsiBuffer.length);
					String imsi = Util.nullTrim(new String(imsiBuffer));

					din.read(ipAddressBuffer, 0, ipAddressBuffer.length);
					String ipAddress = Util.nullTrim(new String(ipAddressBuffer));
					
					din.read(nameBuffer, 0, nameBuffer.length);
					String name = Util.nullTrim(new String(nameBuffer));

					//				int jobNo = byteToInt(toBytes(din.readInt()), ByteOrder.BIG_ENDIAN);
					int jobNo = din.readInt();

					if(CommandManager.getInstance().isLogFlag()) {
						logger.info("===============================================");
						logger.info("MMIB -> PROVS");
						logger.info("bodyLen : " + reservedMsgSize);
						logger.info("command : " + command);
						logger.info("imsi : " + imsi);
						logger.info("ipAddress : " + ipAddress);
						logger.info("name : " + name);
						logger.info("jobNo : " + jobNo);
						logger.info("===============================================");
					}

					//				Thread.sleep(500);
					receiver.receiveMessage(command, imsi, ipAddress, name, jobNo);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}
			msgReadStarted = false;

		} else if (totalReadSize + currentReadSize > reservedMsgSize) {
			throw new IOException("It is never occured, but...");
		} else {
			totalReadSize += currentReadSize;
		}
	}

	@Override
	protected boolean sendMessage(String result) {
		// TODO Auto-generated method stub
		return false;
	}
}