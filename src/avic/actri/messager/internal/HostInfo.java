/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

import java.net.InetAddress;

/**
 * <pre>
 * 客户描述
 * @author tdan
 * 
 * </pre>
 */
public class HostInfo implements IHostInfo {

	private static final long serialVersionUID = 2293863919559465180L;

	private String hostName = null;

	private String hostIp = null;

	private int port = IConstants.PORT_BINDING;// 暂时使用固定的端口，留下待以后扩展为可配置式的

	public HostInfo(InetAddress address, int port) {
		this.hostName = address.getHostAddress();
		this.hostIp = address.getHostAddress();
		this.port = port;
	}

	public HostInfo(String hostName, String hostIp, int port) {
		this.hostName = hostName;
		this.hostIp = hostIp;
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.IHostInfo#getHostIP()
	 */
	public String getHostIP() {
		return hostIp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.IHostInfo#getHostName()
	 */
	public String getHostName() {
		return hostName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see avic1.actri.tools.etalk.IHostInfo#getHostPort()
	 */
	public int getHostPort() {
		return port;
	}

	public String toString() {
		return hostName + " " + hostIp;
	}

	public String[] toStringArray() {
		return new String[] { hostName, hostIp };
	}

	public boolean equals(Object obj) {
		if (obj instanceof IHostInfo) {
			IHostInfo toComp = ((IHostInfo) obj);
			if ((toComp.getHostName() != null && toComp.getHostName().equals(
					hostName))
					&& toComp.getHostIP() != null
					&& toComp.getHostIP().equals(hostIp)
			// && toComp.getHostPort() == port
			) {
				return true;
			}
		}
		return false;
	}
}
