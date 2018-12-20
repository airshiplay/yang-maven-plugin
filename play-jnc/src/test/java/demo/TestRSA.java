package demo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@SuppressWarnings("unused")
public class TestRSA {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    public static void main(String[] args) {
        Map<String, Object> keyMap;
        try {
            keyMap = initKey();
            String publicKey = getPublicKey(keyMap);
            System.out.println(publicKey);
            String privateKey = getPrivateKey(keyMap);
            System.out.println(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        byte[] publicKey = key.getEncoded();
        return encryptBASE64(key.getEncoded());
    }

    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        byte[] privateKey = key.getEncoded();
        return encryptBASE64(key.getEncoded());
    }

    public static byte[] decryptBASE64(String key) throws Exception {
//        Decoder decoder = Base64.getDecoder();
        return DatatypeConverter.parseBase64Binary(key);
    }

    public static String encryptBASE64(byte[] key) throws Exception {
//        Encoder encoder = Base64.getEncoder();

        return DatatypeConverter.printBase64Binary(key);

    }

    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /*
     * CN(Common Name名字与姓氏)
     * OU(Organization Unit组织单位名称)
     * O(Organization组织名称)
     * ST(State州或省份名称)
     * C(Country国家名称)
     * L(Locality城市或区域名称)
     * */
    public static X509Certificate getCert() throws Exception {
        //证书颁发者
        String CertificateIssuer = "C=中国,ST=广东,L=广州,O=人民组织,OU=人民单位,CN=人民颁发";
        //证书使用者
        String CertificateUser = "C=中国,ST=广东,L=广州,O=人民组织,OU=人民单位,CN=";

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        X509V3CertificateGenerator x509V3CertificateGenerator = new X509V3CertificateGenerator();
//设置证书序列号
        x509V3CertificateGenerator.setSerialNumber(BigInteger.TEN);
//设置证书颁发者
        x509V3CertificateGenerator.setIssuerDN(new X500Principal(CertificateIssuer));
//设置证书使用者
        LdapName ldapName = new LdapName(CertificateUser + "sun");
        List<Rdn> rdns = ldapName.getRdns();

        x509V3CertificateGenerator.setSubjectDN(new X500Principal(CertificateUser + "sun"));
//设置证书有效期
        x509V3CertificateGenerator.setNotAfter(new Date(System.currentTimeMillis() + 1000 * 365 * 24 * 3600));
        x509V3CertificateGenerator.setNotBefore(new Date(System.currentTimeMillis()));
//设置证书签名算法
        x509V3CertificateGenerator.setSignatureAlgorithm("SHA1withRSA");


        x509V3CertificateGenerator.setPublicKey(publicKey);

//临时bc方法添加都环境变量
        Security.addProvider(new BouncyCastleProvider());
        X509Certificate x509Certificate = x509V3CertificateGenerator.generateX509Certificate(keyPair.getPrivate(), "BC");
        x509Certificate.getSubjectDN();
//写入文件
        FileOutputStream fos = new FileOutputStream("F:\\cer.cer");
        fos.write(x509Certificate.getEncoded());
        fos.flush();
        fos.close();
        return x509Certificate;
    }

    //读取证书文件获取公钥
    public static void readCer() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate =
                certificateFactory.generateCertificate(new FileInputStream("f:\\demo.cer"));

        PublicKey publicKey = certificate.getPublicKey();
        System.out.println("证书中的公钥:" + DatatypeConverter.printBase64Binary(publicKey.getEncoded()));
    }

    //读取jks文件获取公、私钥
    public static void readJks() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream("f:\\demo.jks"), "123456".toCharArray());
        Enumeration<String> aliases = keyStore.aliases();
        String alias = null;
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
        }
        System.out.println("jks文件别名是：" + alias);
        PrivateKey key = (PrivateKey) keyStore.getKey(alias, "123456".toCharArray());
        System.out.println("jks文件中的私钥是：" + DatatypeConverter.printBase64Binary(key.getEncoded()));
        Certificate certificate = keyStore.getCertificate(alias);
        PublicKey publicKey = certificate.getPublicKey();
        System.out.println("jks文件中的公钥:" + DatatypeConverter.printBase64Binary(publicKey.getEncoded()));
    }
}