/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

import java.nio.ByteBuffer;

import avic.actri.messager.internal.IConstants.Command;

/**
 * <pre>
 * 命令中心
 * 命令结构为
 * 命令类型	用户名	命令消息
 *  0-3     4-35     36-...1024(字节)貌似数据报最大是1024
 * </pre>
 * 
 * @author tdan
 */
public class CommandCenter {

	/**
	 * 获得通知其他人自己上线的命令
	 * 
	 * @return
	 */
	public static byte[] notifyOnLineCommand() {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN);
		buffer.putInt(Command.ONLINE.ordinal());
		buffer.put(getLocalInfo());
		return buffer.array();
	}

	/**
	 * 获得通知其他人自己下线的命令
	 * 
	 * @return
	 */
	public static byte[] notifyOffLineCommand() {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN);
		buffer.putInt(Command.OFFLINE.ordinal());
		buffer.put(getLocalInfo());
		return buffer.array();
	}

	/**
	 * 根据message获得要发送的命令
	 * 
	 * @param message
	 *            要发送的消息
	 * @param length
	 *            要发送的消息长度
	 * @return 要发送的命令
	 */
	public static byte[] sendMessageCommand(byte[] message, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN + length);
		buffer.putInt(Command.MESSAGE.ordinal());
		buffer.put(getLocalInfo());
		buffer.put(message, 0, length);
		return buffer.array();
	}

	/**
	 * 根据message获得要发送的命令
	 * 
	 * @param message
	 *            要发送的消息
	 * @return
	 */
	public static byte[] sendMessageCommand(String message) {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN
				+ message.getBytes().length);
		buffer.putInt(Command.MESSAGE.ordinal());
		buffer.put(getLocalInfo());
		buffer.put(message.getBytes());
		return buffer.array();
	}

	/**
	 * 广播更新自己的状态
	 * 
	 * @return
	 */
	public static byte[] UpdateCommand() {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN);
		buffer.putInt(Command.UPDATE.ordinal());
		buffer.put(getLocalInfo());
		return buffer.array();
	}

	/**
	 * 发送文件的命令
	 * 
	 * @param filePath
	 *            文件列表
	 * @return
	 */
	public static byte[] sendFileCommand(String filePath) {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN
				+ filePath.getBytes().length);
		buffer.putInt(Command.FILE.ordinal());
		buffer.put(getLocalInfo());
		buffer.put(filePath.getBytes());
		return buffer.array();
	}

	/**
	 * 获得本地用户信息
	 * 
	 * @return
	 */
	private static byte[] getLocalInfo() {
		byte[] nameByte = new byte[IConstants.HOSTNAME_LEN];
		IHostInfo localHost = LocalUser.getLocalUser().getHostInfo();
		byte[] name = localHost.getHostName().getBytes();
		for (int i = 0; i < name.length; i++) {
			nameByte[i] = name[i];
		}
		return nameByte;
	}
}
