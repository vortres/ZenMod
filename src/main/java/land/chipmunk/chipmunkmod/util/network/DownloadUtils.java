package land.chipmunk.chipmunkmod.util.network;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class DownloadUtils {
  private static class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) {}

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }

  public static byte[] DownloadToByteArray(URL url) throws IOException, KeyManagementException, NoSuchAlgorithmException {
    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
    SSLContext.setDefault(ctx);
    URLConnection conn = url.openConnection();
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(10000);
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");

    try (BufferedInputStream downloadStream = new BufferedInputStream(conn.getInputStream())) {
      ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = downloadStream.read(buf)) > 0) {
        byteArrayStream.write(buf, 0, n);

        if (Thread.interrupted()) {
          return null;
        }
      }
      return byteArrayStream.toByteArray();
    }
    // Closing a ByteArrayInputStream has no effect, so I do not close it.
  }

  public static InputStream DownloadToInputStream(URL url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
    return new ByteArrayInputStream(DownloadToByteArray(url));
  }
}
