package com.linkcircle.ss;

import java.io.*;
import java.net.URL;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/11/15 16:44
 */

public class A {
    private String encryptFolder = "encrypt";
    public void a(String name) {
        String path = c(name);
        File classFile = new File(path);
        if (!classFile.exists()) {
        } else {
            File folder = new File(classFile.getParent() + File.separator + encryptFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
        String cipheredClass = classFile.getParent() + File.separator + encryptFolder + File.separator + classFile.getName();
        try (
                FileInputStream fileInputStream = new FileInputStream(classFile);
                BufferedInputStream bis = new BufferedInputStream(fileInputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(cipheredClass);
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)
        ) {
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data ^ 0xFF);
            }
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        classFile.delete();
        File oldFile = new File(path + "en");
        if (oldFile.exists()) {
            oldFile.delete();
        }
        File cipheredFile = new File(cipheredClass);
        cipheredFile.renameTo(oldFile);
        cipheredFile.getParentFile().delete();
    }
    public byte[] b(String name) {
        InputStream path = null;
        if (!name.contains(".class")) {
            path = d(name);
        }
        byte[] result = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;
        try {
            bis = new BufferedInputStream(path);
            bos = new ByteArrayOutputStream();
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data ^ 0xFF);
            }
            bos.flush();
            result = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    private String c(String name) {
        String path;
        String str = name.substring(name.lastIndexOf(".") + 1, name.length()) + ".class";
        path = A.class.getResource(str).toString();
        path = path.substring(path.indexOf("file:/") + "file:/".length(), path.length());
        if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
            path = File.separator + path;
        }
        return path;
    }
    private InputStream d(String name) {
        try{
            String path;
            String str = name.substring(name.lastIndexOf(".") + 1, name.length()) + ".classen";
            URL url = A.class.getResource(str);
            InputStream is = (InputStream)url.getContent();
            return is;
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
    public static void main(String[] args) throws Exception{
        A a = new A();
        a.a("com.linkcircle.ss.D");
//        byte[] bytes = edCipher.decryptClass("com.linkcircle.druid.Ss");
//        System.out.println(bytes.length);
    }
}
