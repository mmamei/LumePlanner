package services.citylive;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.zip.ZipFile;

/**
 * Created by marco on 13/04/2017.
 */
public class DataPipeDownload {
        public static void main(String[] args) throws Exception {
            DataPipeDownload td = new DataPipeDownload();
            td.download();
        }

        private static final String authFile = "D:/datapipe-auth.txt";
        private static final String tmpFile = "D:/data.zip";
        private static final String outDir = "D:/LUME-ER";

        public void download() {
            try {

                String auth = new BufferedReader(new FileReader(authFile)).readLine();
                disableCertificateValidation();

                URL obj = new URL("https://easyapi.telecomitalia.it:8248/mobilityscan/v1/location?regione=Emilia-Romagna");
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("Authorization", "Bearer "+auth);

                //int responseCode = con.getResponseCode();
                //System.out.println("Response Code : " + responseCode);

                InputStream is = con.getInputStream();
                OutputStream outStream = new FileOutputStream(tmpFile);

                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(outStream);

                ZipFile zipFile = new ZipFile(tmpFile);
                String file = zipFile.entries().nextElement().getName().replaceAll("hdr|bil", "zip");
                System.out.println(file);
                File out = new File(outDir + "/" + file);
                if (!out.exists())
                    Files.copy(Paths.get(tmpFile), Paths.get(out.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public static void disableCertificateValidation() {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }};

            // Ignore differences between given hostname and certificate hostname
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            };

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            } catch (Exception e) {}
        }
}
