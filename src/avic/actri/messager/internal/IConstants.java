/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

/**
 * 常量集合
 * 
 * @author tdan
 */
public interface IConstants {
	public static final int PORT_BINDING = 9527;// 本机发送时，需要binding的端口
	public static final int PORT_LISTEN = 9528;// 本机监听接收数据的端口,发送数据的端口
	public static final int PORT_MUTI_BINDING = 9529;// 本机发送时，需要binding的端口
	public static final int PORT_MUTI_LISTEN = 9530;// 本机监听接收数据的端口,发送数据的端口
	public static final int PORT_SEND_FILE = 9531;// 发送文件端口
	public static final int PORT_SEND_CONTRAL = 9532;// 发送文件控制端口

	public static final int MAX_MSG_BYTE = 1024;// 最大消息长度

	public static final String BROADCAST_ADDRESS = "230.0.0.1";// 广播地址

	public static final String DATA_HOST = "host";// 为tableItem设置数据的关键字,存储对应的客户信息

	public static final int SEND_ACCEPT = 1;// 同意接收

	public static final int SEND_REFUZE = 0;// 拒绝接收

	public static final int SEND_BYTE_SIZE = 1024;// 发送文件时，一次发送的字节数

	public static final String SEND_FILE = "发送文件";

	public static final String SEND_FILE_DIV = ",";// 多文件划分点

	public static final String SEND_FILE_SIZE_DIV = "#";// 文件名与大小的划分点

	public static final int HOSTNAME_LEN = 32;// 主机名长度

	public static final int MSG_BEGIN = 36;// 消息中消息头长度，消息标记+用户名

	public enum Command {
		ONLINE, // 上线通知，获得其他Host的状态
		OFFLINE, // 下线通知
		MESSAGE, // 发送消息
		FILE, // 发送文件
		UPDATE, // 更新状态,通知自己的状态
	}
}
