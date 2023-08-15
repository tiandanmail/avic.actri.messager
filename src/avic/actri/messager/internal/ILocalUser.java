/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

/**
 * 本地用户描述
 * 
 * @author tdan
 */
public interface ILocalUser {

	/**
	 * 通知大家上线
	 *
	 */
	public void notifyOnline();

	/**
	 * 通知大家下线
	 *
	 */
	public void notifyOffline();

	/**
	 * 向toHost发送消息message
	 */
	public void sendMessage(IHostInfo toHost, String message);

	/**
	 * 向toHost发送消息message
	 */
	public void sendFile(IHostInfo toHost, String message);

	/**
	 * 获得用户信息
	 */
	public IHostInfo getHostInfo();

	/**
	 * 广播发送自己在线的消息
	 */
	public void upDate();

	/**
	 * 向toHost发送自己在线的消息
	 */
	public void upDate(IHostInfo toHost);

}
