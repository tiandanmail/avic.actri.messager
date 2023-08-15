/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

import java.nio.ByteBuffer;

import avic.actri.messager.internal.IConstants.Command;

/**
 * <pre>
 * ��������
 * ����ṹΪ
 * ��������	�û���	������Ϣ
 *  0-3     4-35     36-...1024(�ֽ�)ò�����ݱ������1024
 * </pre>
 * 
 * @author tdan
 */
public class CommandCenter {

	/**
	 * ���֪ͨ�������Լ����ߵ�����
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
	 * ���֪ͨ�������Լ����ߵ�����
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
	 * ����message���Ҫ���͵�����
	 * 
	 * @param message
	 *            Ҫ���͵���Ϣ
	 * @param length
	 *            Ҫ���͵���Ϣ����
	 * @return Ҫ���͵�����
	 */
	public static byte[] sendMessageCommand(byte[] message, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(IConstants.MSG_BEGIN + length);
		buffer.putInt(Command.MESSAGE.ordinal());
		buffer.put(getLocalInfo());
		buffer.put(message, 0, length);
		return buffer.array();
	}

	/**
	 * ����message���Ҫ���͵�����
	 * 
	 * @param message
	 *            Ҫ���͵���Ϣ
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
	 * �㲥�����Լ���״̬
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
	 * �����ļ�������
	 * 
	 * @param filePath
	 *            �ļ��б�
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
	 * ��ñ����û���Ϣ
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
