/**
 * @copyright actri.avic
 */
package avic.actri.messager.internal;

import java.io.Serializable;

/**
 * �ͻ�����
 * 
 * @author tdan
 */
public interface IHostInfo extends Serializable {

	public String getHostName();

	public String getHostIP();

	public int getHostPort();
}
