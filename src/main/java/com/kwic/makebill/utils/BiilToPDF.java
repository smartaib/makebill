package com.kwic.makebill.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * makebill Library
 * Class: BiilToPDF
 * Created by dongmoon.Kwon on 2021/07/08.
 * <p>
 * Copyright (c) 2021 KWIC Co., Ltd.
 * All rights reserved.
 */

public class BiilToPDF {

    public static final String TAG = "BiilToPDF";

    private Context context = null;
    private PDFUtil util = null;
    private Map<String, String> bItemEntry;
    private PdfWriter writer = null;
    private PdfContentByte cb;
    private ByteArrayOutputStream stream = null;
    private PdfReader reader = null;
    Document document = null;
    private int heightDoc;

    public BiilToPDF(Map<String, String> ItemEntry, Context context) {
        this.context = context;
        this.bItemEntry = ItemEntry;
        util = new PDFUtil(context);
    }

    public BiilToPDF(Context context) {
        this.context = context;
        util = new PDFUtil(context);
    }

    public PdfWriter getWriter() {
        return writer;
    }

    public PDFUtil getUtil() {
        return util;
    }

    public Map<String, String> getItem() {
        return bItemEntry;
    }

    public boolean compressionBill(String iFile, String oFile) {

        boolean ret = true;
        try {
            PdfReader reader = new PdfReader(iFile);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(oFile), PdfWriter.VERSION_1_7);
            stamper.getWriter().setCompressionLevel(9);
            int total = reader.getNumberOfPages() + 1;
            for (int i = 1; i < total; i++) {
                reader.setPageContent(i, reader.getPageContent(i));
            }
            stamper.setFullCompression();
            stamper.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }

    public boolean makeCompletePDF(String basePath, ArrayList<String> imageFiles) {

        boolean ret = true;
        Document document		= new Document(PageSize.A4);
        FileOutputStream fos	= null;
        try{

            if(new File(basePath).exists()) {
                removeFile(basePath);
            }

            fos	= new FileOutputStream(basePath);
            writer	= PdfWriter.getInstance(document, fos);
            document.open();

            for (String imageFile : imageFiles) {

                File f = new File(imageFile);
                if (!f.exists()) continue;

//                Image image = Image.getInstance(imageFile);
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile);

                if (bitmap == null)
                    return false;

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                Image image = Image.getInstance(stream.toByteArray());
                image.setAbsolutePosition(0, 0);

                float scaler = (document.getPageSize().getWidth() / image.getWidth()) * 100;
                image.scalePercent(scaler);

                document.newPage();
                document.add(image);
            }
        }catch (DocumentException e){
            CustomLog.e(e.getMessage());
            e.printStackTrace();
            ret = false;
        }catch (FileNotFoundException e){
            CustomLog.e(e.getMessage());
            e.printStackTrace();
            ret = false;
        }catch (Exception e){
            CustomLog.e(e.getMessage());
            e.printStackTrace();
            ret = false;
        }finally{
            try{
                document.close();
            }catch(Exception ignored){}
			/*try{if(writer!=null)writer.close();}catch(Exception ex){}
			try{if(reader!=null)reader.close();}catch(Exception ex){}*/
        }
        return ret;
    }

    public void saveToFile(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (NullPointerException e) {
            CustomLog.e(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            CustomLog.e(e.getMessage());
        } catch (ClassCastException e) {
            CustomLog.e(e.getMessage());
        } catch (Exception e) {
            CustomLog.e(e.getMessage());
        }finally{
            try {
                out.close();
            } catch (IOException e) {
                CustomLog.e(e.getMessage());
            }
        }
    }

    public void removeFile(String opath) throws Exception{
        File file	= new File(opath);
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean makeBillPDF(String iFilePath, String oFilePath, JSONArray locationDatas) {

        boolean ret = true;

        CustomLog.d("makeBillPDF :: iFile=" + iFilePath + ", oFile=" + oFilePath);

        try {
            document = new Document(PageSize.A4);
            writer = PdfWriter.getInstance(document, new FileOutputStream(oFilePath));
            document.open();
            heightDoc = (int) document.getPageSize().getHeight();
            cb = writer.getDirectContent();
            reader = new PdfReader(iFilePath);

            for (int i = 0; i < locationDatas.length(); i++) {

                if (reader.getNumberOfPages() <= i)
                    continue;

                PdfImportedPage page1 = writer.getImportedPage(reader, i + 1);
                page1.setWidth(document.getPageSize().getWidth());
                page1.setHeight(heightDoc);
                CustomLog.d("PDF(" + iFilePath + ")page size (" + page1.getWidth() + ", " +page1.getHeight() + ")" );
                if (page1 != null) {
                    document.newPage();
                    cb.addTemplate(page1, 0, 0);

                    JSONArray locations = getJSONArrayParse(locationDatas, i);

                    for (int j = 0; j < locations.length(); j++) {
                        JSONObject location = (JSONObject) locations.get(j);
                        CustomLog.d("JSONObject=" + location.toString());

                        String type = (String) location.getString("TYPE");
                        switch (type) {
                            case "CHAR":
                            case "HPCHAR":
                            case "STRING":
                            default:
                                drawString(location);
                                break;
                            case "SIGN":
                                drawSign(location);
                                break;
                            case "ACCTSTR":
                                drawWithdrawal(location);
                                break;
                            case "LISTRELATION":
                                drawRelationship(location);
                                break;
                            case "CHECK":
                                drawCheck(location);
                                break;
                            case "DATETYPE_A":
                            case "DATETYPE_B":
                            case "DATETYPE_C":
                                drawDate(location);
                                break;
                            case "EMAIL_SPLIT":
                                drawEmail(location);
                                break;
                        }

                    }
                }

            }
        } catch (DocumentException e) {
            e.printStackTrace();
            CustomLog.e(e.getMessage());
            ret = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            CustomLog.e(e.getMessage());
            ret = false;
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(e.getMessage());
            ret = false;
        } finally {
            try {
                if (document != null) document.close();
            } catch (Exception ex) {
            }
            try {
                if (writer != null) writer.close();
            } catch (Exception ex) {
            }
            try {
                if (reader != null) reader.close();
            } catch (Exception ex) {
            }
            try {
                if (stream != null) stream.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private void drawString(JSONObject data) throws JSONException {
        String code = data.getString("CODE");
        String value = "";
        String pJumin[] = make_jumin(getItem().get("p_jumin")).split("-");
        String cJumin[] = make_jumin(getItem().get("c_jumin")).split("-");

        switch (code) {
            case "contract.name":
                value = getItem().get("c_name");
                break;
            case "contract.encJumin1":
                value = cJumin[0];
                break;
            case "contract.encJumin2":
                value = cJumin[1];
                break;
            case "contract.job":
            case "insured.job":
                value = getItem().get("p_job");
                break;
            case "insured.name":
                value = getItem().get("p_name");
                break;
            case "insured.encJumin1":
                value = pJumin[0];
                break;
            case "insured.encJumin2":
                value = pJumin[1];
                break;
            case "insured.hpNumber":
            case "insured.telNumber":
                value = getItem().get("g_hp");
                break;
            case "insured.email":
                value = getItem().get("g_email");
                break;
            case "insured.companyName":
                value = getItem().get("p_work");
                if (value.length() == 0)
                    value = " ";
                break;
            case "insuredAdditional.fatherName":
                if (getItem().get("agree_name1").length() > 0)
                    value = getItem().get("agree_name1");
                break;
            case "insuredAdditional.motherName":
                if (getItem().get("agree_name2").length() > 0)
                    value = getItem().get("agree_name2");
                break;
            case "requirer.name":
                value = getItem().get("c_name");
                break;
            case "requirer.encJumin1":
                value = cJumin[0];
                break;
            case "requirer.encJumin2":
                value = cJumin[1];
                break;
            case "requirer.job":
                value = getItem().get("p_job");
                break;
            case "requirer.hpNumber":
            case "requirer.telNumber":
                value = getItem().get("g_hp");
                break;
            case "requirer.email":
                value = getItem().get("g_email");
                break;
            case "requirer.issuer_name":
                value = getItem().get("jumin_apparatus");
                break;
            case "receivingInsurance.bankName":
                if ("1".equals(getItem().get("is_cms")))
                    return;
                value = getItem().get("bank_name");
                break;
            case "receivingInsurance.bankOwner":
                if ("1".equals(getItem().get("is_cms")))
                    return;
                value = getItem().get("bank_owner");
                break;
            case "receivingInsurance.bankAccountNo":
                if ("1".equals(getItem().get("is_cms")))
                    return;
                value = getItem().get("bank_account");
                break;
            case "invoice.treatment_org":
                value = getItem().get("acc_host1");
                break;
            case "invoice.treatment_reason":
                value = getItem().get("acc_dig");
                break;
            case "etc.requisition_date": // 청구일
            case "invoice.treatment_date":
            case "requirer.issuer_date":
                drawDate(data);
                return;
            default:
                return;
        }

        try {
            CustomLog.d("type=" + code + "value=" + value);
            int size = data.getInt("SIZE");
            JSONArray COORDINATES = (JSONArray) data.get("COORDINATE");

            if (COORDINATES.length() > 1 && COORDINATES.length() >= value.length()) {
                String[] arr = value.split("(?<!^)");
                for (int i = 0; i < COORDINATES.length() && i < arr.length; i++) {
                    JSONObject COORDINATE = (JSONObject) COORDINATES.get(i);
                    int x = COORDINATE.getInt("X");
                    int y = heightDoc - COORDINATE.getInt("Y");

//                    if (arr[i] != null)
                        getUtil().absText(getWriter(), arr[i], x, y, size);
                }
            } else {
                JSONObject COORDINATE = (JSONObject) COORDINATES.get(0);
                int x = COORDINATE.getInt("X");
                int y = heightDoc - COORDINATE.getInt("Y");

                getUtil().absText(getWriter(), value, x, y, size);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawSign(JSONObject data) throws JSONException {
        String code = data.getString("CODE");

        String pName = getItem().get("p_name");
        String cName = getItem().get("c_name");

        Bitmap pImage = null;
        String signPath = "";

        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            switch (code) {
                case "contract.sign":
                case "requirer.sign":
                    signPath = getItem().get("sign");
                    pImage = BitmapFactory.decodeFile(signPath);
                    break;
                case "insured.sign":
                    signPath = getItem().get("sign");
                    if (!pName.equals(cName)) {
                        signPath = getItem().get("sign1");
                    }
                    pImage = BitmapFactory.decodeFile(signPath);
                    break;
                case "insuredAdditional.fatherSign":
                    if (getItem().get("agree_name1").length() == 0)
                        return;
                    signPath = getItem().get("agree_sign1");
                    pImage = BitmapFactory.decodeFile(signPath);
                    break;
                case "insuredAdditional.motherSign":
                    if (getItem().get("agree_name2").length() == 0)
                        return;
                    signPath = getItem().get("agree_sign2");
                    pImage = BitmapFactory.decodeFile(signPath);
                    break;
                default:
                    return;
            }

            JSONObject COORDINATE = (JSONObject) ((JSONArray) data.get("COORDINATE")).get(0);
            int x = COORDINATE.getInt("X");
            int y = COORDINATE.getInt("Y");


            CustomLog.d("x=" + x + ", y=" + y + ", pImage.getHeight()="+pImage.getHeight() + ", heightDoc="+heightDoc);
            pImage = Bitmap.createScaledBitmap(pImage, pImage.getWidth(), pImage.getHeight(), true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
            pImage.recycle();
            byte[] byteArray = baos.toByteArray();

            y = heightDoc - y - 25;
            Image img = Image.getInstance(byteArray);

            img.setAbsolutePosition(x, y);
            img.scaleToFit(100, 50);
            cb.addImage(img);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void drawWithdrawal(JSONObject data) throws JSONException {
        String code = data.getString("CODE");
        switch (code) {
            case "receivingInsurance.withdrawal": // 자동이체 여부
                if (!"1".equals(getItem().get("is_cms")))
                    return;
                break;
            default:
                return;
        }
        int size = data.getInt("SIZE");
        JSONObject COORDINATE = (JSONObject) ((JSONArray) data.get("COORDINATE")).get(0);
        int x = COORDINATE.getInt("X");
        int y = heightDoc - COORDINATE.getInt("Y");

        getUtil().absText(getWriter(), "자동이체", x, y, size);
    }

    private void drawRelationship(JSONObject data) throws JSONException {
        String code = data.getString("CODE");
        String[] relationshops = {
                "본인",
                "배우자",
                "부모",
                "자녀",
                "형제",
                "자매",
                "기타"
        };

        String value = "";
        switch (code) {
            case "requirer.relationship":
                int index = Integer.parseInt(getItem().get("relationship"));
                if (index == 99)
                    index = 7;
                value = relationshops[--index];
                break;
            default:
                return;
        }

        int size = data.getInt("SIZE");
        JSONObject COORDINATE = (JSONObject) ((JSONArray) data.get("COORDINATE")).get(0);
        int x = COORDINATE.getInt("X");
        int y = heightDoc - COORDINATE.getInt("Y");

        getUtil().absText(getWriter(), value, x, y, size);
    }

    private void drawCheck(JSONObject data) throws JSONException {
        String code = data.getString("CODE");
        switch (code) {
            case "etc.injury": // 질병 체크
                if (getItem().get("accident").equals("0"))
                    return;
                break;
            case "etc.check":
                break;
            case "etc.disease": // 상해 체크
                if (getItem().get("accident").equals("1"))
                    return;
                break;
            default:
                return;
        }
        int size = data.getInt("SIZE");
        JSONObject COORDINATE = (JSONObject) ((JSONArray) data.get("COORDINATE")).get(0);
        int x = COORDINATE.getInt("X");
        int y = heightDoc - COORDINATE.getInt("Y");

        getUtil().absText(getWriter(), "V", x, y, size);
    }

    private void drawDate(JSONObject data) throws JSONException {
        String code = data.getString("CODE");
        String type = data.getString("TYPE");
        String value = "";
        switch (code) {
            case "etc.requisition_date": // 청구일
                java.util.Date d = new java.util.Date();
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                value = df.format(d);

//                _today = today.split("-");
                break;
            case "invoice.treatment_date":
                value = getItem().get("acc_date");
                break;
            case "requirer.issuer_date":
                value = getItem().get("jumin_issue");
                break;
            default:
                return;
        }


        switch (type) {
            case "DATETYPE_A":
                break;
            case "DATETYPE_B":
                CustomLog.d("value=" + value);
                if (value.contains("-")) {
                    value = value.replace("-", "");
                }
                if (value.length() == 0)
                    return;
                value = value.substring(2,8);
                break;
            case "DATETYPE_C":
                break;
            default:
        }

        if (value.length() == 0)
            return;

        String[] days = null;

        try {
            CustomLog.d("value=" + value);
            int size = data.getInt("SIZE");
            JSONArray COORDINATES = (JSONArray) data.get("COORDINATE");

            if (COORDINATES.length() == 1) {
                days = new String[]{value};
            } else if (COORDINATES.length() == 3) {
                if (value.contains("-"))
                    days = value.split("-");
                else if (value.length() == 6)
                    days = new String[]{value.substring(0, 2), value.substring(2, 4), value.substring(4, 6)};
                else if (value.length() == 8)
                    days = new String[]{value.substring(0, 4), value.substring(4, 6), value.substring(6, 8)};
            } else if (COORDINATES.length() == 6) {
                if (value.contains("-")) {
                    value = value.replace("-", "");
                }

                if (value.length() == 6) {
                    days = new String[]{value.substring(0, 1), value.substring(1, 2),
                            value.substring(2, 3), value.substring(3, 4),
                            value.substring(4, 5), value.substring(5, 6)};
                }
                else {
                    days = new String[]{value.substring(2, 3), value.substring(3, 4),
                            value.substring(4, 5), value.substring(5, 6),
                            value.substring(6, 7), value.substring(7, 8)};
                }
            } else if (COORDINATES.length() == 8) {
                if (value.contains("-"))
                    value = value.replace("-", "");
                days = value.split("(?<!^)");

            }

            for (int i = 0; i < COORDINATES.length(); i++) {
                JSONObject COORDINATE = (JSONObject) COORDINATES.get(i);
                int x = COORDINATE.getInt("X");
                int y = heightDoc - COORDINATE.getInt("Y");

                getUtil().absText(getWriter(), days[i], x, y, size);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawEmail(JSONObject data) throws JSONException {
        String code = data.getString("CODE");
        String value = "";
        switch (code) {
            case "insured.email":
                value = getItem().get("g_email");
                break;
            case "requirer.email":
                value = getItem().get("g_email");
                break;
            default:
                return;
        }

        String[] emails = value.split("@");
        int size = data.getInt("SIZE");
        JSONArray COORDINATES = (JSONArray) data.get("COORDINATE");
        for (int i = 0; i < COORDINATES.length(); i++) {
            JSONObject COORDINATE = (JSONObject) COORDINATES.get(i);
            int x = COORDINATE.getInt("X");
            int y = heightDoc - COORDINATE.getInt("Y");

            getUtil().absText(getWriter(), emails[i], x, y, size);
        }
    }

    public JSONArray getJSONArrayParse(JSONArray obj, int i) throws JSONException {
        if (obj == null) {
            return null;
        }

        if (obj.length() > i) {
            return (obj.get(i) instanceof JSONArray) ? obj.getJSONArray(i) : null;
        }
        return null;
    }

    public static String make_jumin(String j) {
        String jumin = "";
        try {
            jumin = j;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jumin.length() == 13) {
            return jumin.substring(0, 6) + "-" + jumin.substring(6);
        } else {
            if (j.length() == 9) {
                return j.substring(2, 8) + "-" + j.substring(8) + "******";
            } else {
                return j;
            }
        }
    }

}
