/**
 * 
 */
package practices.microservice.common;

/**
 * @author bwang
 *
 */
public class Constants {

	public enum RpcServerEnum {
		
		DEFAULT_CONFIG(8443, true);
		
		private boolean useTls = false;

		private int port;
		
		private RpcServerEnum(int pPort, boolean pUseTls){
			port = pPort;
			useTls = pUseTls;
		}
		@Override
        public String toString() {
            return this.port + ":" + this.useTls;
        }
		
		public boolean isUseTls(){
			return this.useTls;
		}
		
		public int getPort(){
			return this.port;
		}
	}
}
