package practices.microservice.rpc.server;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.utils.Constants;


/**
 * RPC Server launcher from the command.
 * 
 * @author bwang
 *
 */
public class ServerBootstrap {

	private static Logger log = LoggerFactory.getLogger(ServerBootstrap.class);
	
	/**
	 * The main application allowing this server to be launched from the command
	 * line.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String[] params = parseArgs(args);
        new ServerBootstrap().boot(params);
	}
	
	public void boot(final String[] params) throws Exception{
		
		log.info("Booting RPC Server...");
		long times = System.currentTimeMillis();	
		echoLicense();
		final RpcServer rpcServer = RpcServerProvider.provider().createServer();
		rpcServer.configure(Integer.parseInt(params[0]), Boolean.valueOf(params[1]));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					log.info("RPC Server shutting down on port {}", params[0]);
					rpcServer.stop();
				} catch (Exception e) {				
					log.error("RPC Server shutting down error", e.getMessage(), e);
				}
			}
		});
		rpcServer.start();
		times = System.currentTimeMillis() - times;
		log.info("RPC Server started on port {} in {} ms", params[0], times);

		rpcServer.blockUntilShutdown();
	}
	
	private static void echoLicense() throws IOException {
		InputStream in = ServerBootstrap.class.getResourceAsStream("/License.txt");

		byte[] buffer = new byte[in.available()];
		in.read(buffer);
		in.close();

		log.info(new String(buffer));
	}

	private static String[] parseArgs(String[] args) {
		boolean usage = false;
		String[] params = new String[2];
		for (String arg : args) {
			if (!arg.startsWith("--")) {
				//System.err.println("All arguments must start with '--': " + arg);
				log.error("All arguments must start with '--': {}", arg);
				usage = true;
				break;
			}
			String[] parts = arg.substring(2).split("=", 2);
			String key = parts[0];
			if ("help".equals(key)) {
				usage = true;
				break;
			}
			if (parts.length != 2) {
//				System.err
//						.println("All arguments must be of the form --arg=value");
				log.error("All arguments must be of the form --arg=value");
				usage = true;
				break;
			}
			String value = parts[1];
			if ("port".equals(key)) {
				params[0] = value;
			} else if ("use_tls".equals(key)) {
				params[1] = value;
			} else {
//				System.err.println("Unknown argument: " + key);
				log.error("Unknown argument: {}", key);
				usage = true;
				break;
			}
		}
		if (usage) {
			log.info("Usage: [ARGS...]" + "\n"
					+ "\n  --port=PORT           Port to connect to. Default "
					+ Constants.RpcServerEnum.DEFAULT_CONFIG.getPort()
					+ "\n  --use_tls=true|false  Whether to use TLS. Default "
					+ Constants.RpcServerEnum.DEFAULT_CONFIG.isUseTls());
			System.exit(1);
		}
		
		return params;
	}
}
