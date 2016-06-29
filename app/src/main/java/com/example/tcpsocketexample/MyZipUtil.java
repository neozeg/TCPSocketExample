package com.example.tcpsocketexample;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Administrator on 2016/6/29.
 */
public class MyZipUtil {
    public final static String FILE_EXT = "\\.\\w{1,}";

    static public byte[] createZipBuffer(String filepath) throws IOException {
        Pattern p = Pattern.compile(FILE_EXT);
        Matcher m = p.matcher(filepath);
        String tempName = m.replaceAll(".temp");
        ZipOutputStream outTemp = new ZipOutputStream(new FileOutputStream(new File(tempName)));
        File srcFile = new File(filepath);
        InputStream in = new FileInputStream(srcFile);
        outTemp.putNextEntry(new ZipEntry(srcFile.getName()));
        int temp = 0;
        while ((temp = in.read())!= -1){
            outTemp.write(temp);
        }
        in.close();
        outTemp.close();
        File zipFile = new File(tempName);
        in = new FileInputStream(zipFile);

        byte[] buffer;
        int len = in.available();
        buffer = new byte[len];
        in.read(buffer);
        in.close();
        zipFile.delete();

        return buffer;
    }
    static public void createZip(String from) throws IOException{
        Pattern p = Pattern.compile(FILE_EXT);
        Matcher matcher = p.matcher(from);
        String to = matcher.replaceAll(".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(to)));
        File file = new File(from);
        InputStream in = new FileInputStream(file);
        out.putNextEntry(new ZipEntry(file.getName()));
        int temp=0;
        while ((temp = in.read())!= -1){
            out.write(temp);
        }
        in.close();
        out.close();
    }
    static public void createZip(String from,String to) throws IOException {
        List<File> list = getFiles(from);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(to)));
        for(File file:list){
            InputStream in = new FileInputStream(file);
            //String name = getRelName(from,file);
            String name = file.getName();
            ZipEntry en  = new ZipEntry(new File(from).getName()+"/"+name);
            en.setSize(file.length());
            out.putNextEntry(en);
            out.setComment("中文测试");
            int len = 0;
            byte[] buffer = new byte[1024];
            while((len=in.read(buffer))!= -1){
                out.write(buffer,0,len);
            }
            in.close();
        }
        out.close();
    }

    static private String getRelName(String from, File file){
        String a = file.getAbsolutePath().replace(from+"\\","");
        a = a.replace("\\","/");
        return a;
    }
    static private List<File> getFiles(String src){
        List<File> list = new ArrayList<File>();
        File f = new File(src);
        File files[] = f.listFiles();
        for(File file:files){
            if(file.isFile()){
                list.add(file);
            }else{
                list.addAll(getFiles(file.getPath()));
            }
        }
        return list;
    }
}
