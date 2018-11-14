package src;
/**
 * a PBE symmetric encryption class
 * reference from https://blog.csdn.net/zhang_zhenwei/article/details/13630361
 */

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


public class PBE_Encrypt {

    byte[] salt = "00000000".getBytes();
    String password = "123456";
    /**
     * 算法
     */
    public static final String ALGORITHM = "PBEWITHMD5andDES";
    /**
     * 迭代次数
     */
    public static int ITERAT_COUNT = 8;

    /**
     * 构造一个8位的盐
     *
     * @return
     */
    private byte[] initSalt() {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(8);
    }

    /**
     * 生成口令的Key
     *
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private Key getKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec);
    }

    /**
     * 加密
     *
     * @param password
     * @param salt
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] encript(String password, byte[] salt, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException {
        Key key = this.getKey(password);
        //实例化PBE参数材料
        PBEParameterSpec params = new PBEParameterSpec(salt, ITERAT_COUNT);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, params);
        return cipher.doFinal(data);
    }

    /**
     * 解密
     *
     * @param password
     * @param salt
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] decript(String password, byte[] salt, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException {
        Key key = this.getKey(password);
        //实例化PBE参数材料
        PBEParameterSpec params = new PBEParameterSpec(salt, ITERAT_COUNT);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher.doFinal(data);
    }

    public String encode(String text) throws Exception{
        byte[] cryptograph = this.encript(password, salt, text.getBytes("UTF-8"));
        String t = Base64.getEncoder().encodeToString(cryptograph);
        return t;
    }

    public String decode(String text) throws Exception{
        byte[] t = Base64.getDecoder().decode(text);
        byte[] data = this.decript(password, salt, t);
        return new String(data, "UTF-8");

    }

    public void test() throws Exception {
        String data = "需要处理的数据";

        byte[] cryptograph = this.encript(password, salt, data.getBytes("UTF-8"));
        byte[] newData = this.decript(password, salt, cryptograph);
        System.out.println(data);
        System.out.println(cryptograph);
        String t = Base64.getEncoder().encodeToString(cryptograph);
        System.out.println(t);
        byte[] t2 = Base64.getDecoder().decode(t);
        System.out.println(t2);
        System.out.println(new String(newData, "UTF-8"));
    }
}