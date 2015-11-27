package sk.styk.martin.bakalarka.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.styk.martin.bakalarka.decompile.ApkUnziper;
import sk.styk.martin.bakalarka.files.FileFinder;
import sun.security.pkcs.PKCS7;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Styk on 25.11.2015.
 */
public class CertificateProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AndroidManifestProcessor.class);
    private static CertificateProcessor instance = null;
    private List<CertificateData> certDatas ;
    private File manifestFile;
    private ApkData data;

    private CertificateProcessor() {
        // Exists only to defeat instantiation.
    }

    public static CertificateProcessor getInstance(ApkData data) {
        if (data == null) {
            throw new IllegalArgumentException("data null");
        }

        if (instance == null) {
            instance = new CertificateProcessor();
        }
        instance.data = data;
        instance.certDatas = new ArrayList<CertificateData>();
        return instance;
    }

    public static CertificateProcessor getInstance() {
        if (instance == null) {
            instance = new CertificateProcessor();
        }
        instance.data = null;
        instance.certDatas = new ArrayList<CertificateData>();
        return instance;
    }

    public List<CertificateData> processCertificates() {
        return processCertificates(new File(ApkUnziper.TEMP_FOLDER_UNZIP + File.separator + "META-INF"));
    }

    public List<CertificateData> processCertificates(File dirWithCertificates) {

        FileFinder ff = new FileFinder(dirWithCertificates);

        List<File> certs = ff.getCertificateFilesInDirectories();

        for(File f : certs){
            InputStream is = null;
            try {
                is = new FileInputStream(f);
                processCertificate(is);
            } catch (FileNotFoundException e) {
                logger.error("Unable to read certificate " + f.getName());
            }finally {
                if(is!=null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        if(data!=null){
            data.setCertificateDatas(certDatas);
        }
        return certDatas;
    }

    private void processCertificate(InputStream is) {

        try {
            PKCS7 pkcs7 = new PKCS7(is);
            X509Certificate[] certificates = pkcs7.getCertificates();

            for (int i = 0; i < certificates.length ; ++i) {
                X509Certificate certificate = certificates[i];
                CertificateData data = new CertificateData();

                byte[] bytes = certificate.getEncoded();
                String certMd5 = this.md5Digest(bytes);
                String publicKeyString = this.byteToHexString(bytes);
                data.setPublicKey(publicKeyString);

                String certBase64Md5 = this.md5Digest(publicKeyString);
                data.setCertBase64Md5(certBase64Md5);
                data.setCertMd5(certMd5);

                data.setStartDate(certificate.getNotBefore());
                data.setEndDate(certificate.getNotAfter());
                data.setSignAlgorithm(certificate.getSigAlgName());
                data.setSignAlgorithmOID(certificate.getSigAlgOID());
                data.setIssuerName(certificate.getIssuerX500Principal().getName());
                data.setVersion(certificate.getVersion());

                certDatas.add(data);
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }

    }
    private String md5Digest(byte[] input) throws IOException {
        MessageDigest digest = this.getDigest("Md5");
        digest.update(input);
        return this.getHexString(digest.digest());
    }

    private String md5Digest(String input) throws IOException {
        MessageDigest digest = this.getDigest("Md5");
        digest.update(input.getBytes(StandardCharsets.UTF_8));
        return this.getHexString(digest.digest());
    }

    private String byteToHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder(bArray.length);
        byte[] var4 = bArray;
        int var5 = bArray.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            byte aBArray = var4[var6];
            String sTemp = Integer.toHexString(255 & (char)aBArray);
            if(sTemp.length() < 2) {
                sb.append(0);
            }

            sb.append(sTemp.toUpperCase());
        }

        return sb.toString();
    }

    private String getHexString(byte[] digest) {
        BigInteger bi = new BigInteger(1, digest);
        return String.format("%032x", new Object[]{bi});
    }

    private MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var3) {
            throw new RuntimeException(var3.getMessage());
        }
    }

}
