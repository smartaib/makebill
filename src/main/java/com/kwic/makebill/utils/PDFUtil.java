package com.kwic.makebill.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Administrator on 14. 3. 5.
 */
public class PDFUtil {
    @SuppressWarnings("unused")
	private Context context = null;
    public static final int WIDTH = 128*4;
    public static final int HEIGHT = 64*4;
    private ImageLoader imageLoader;

    public PDFUtil(Context mContext){
        context = mContext;
    }

    public ImageLoader getImageLoader() {
        if (imageLoader == null) {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                    .bitmapConfig(Bitmap.Config.ARGB_8888).build();
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(defaultOptions).memoryCache(new WeakMemoryCache());

            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        }

        return imageLoader;
    }

    public void attachImage(String inFile, String outFile, ArrayList<String> attatchs) {
        Document document		= new Document(PageSize.A4);
        PdfWriter writer 		= null;
        PdfReader reader		= null;
        PdfContentByte cb		= null;
        FileInputStream is		= null;
        FileOutputStream fos	= null;
        PdfImportedPage page	= null;
        File file	= null;
        try{
            file	= new File(inFile);
            fos		= new FileOutputStream(outFile);
            is		= new FileInputStream(file);
            writer	= PdfWriter.getInstance(document, fos);
            document.open();
            cb = writer.getDirectContent();
            reader = new PdfReader(is);

            for (int i=1;i<=reader.getNumberOfPages();i++) {
                page = writer.getImportedPage(reader, i);
                document.newPage();
                cb.addTemplate(page, 0, 0);
            }

            for (int i=0;i<attatchs.size();i++) {
                String filePath = attatchs.get(i);
                if(filePath.indexOf("add_") >= 0)
                    continue;

                File f = new File(filePath);
                if(!f.exists()) continue;

                // [ 768 0.9M 정도의 이미지 파일 ]
                int width = 768;
                int height = 768;
                //5.0이상 ..
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    width = 768;
                    height = 768;
                }

                ImageSize targetSize = new ImageSize(width, height);
                ImageLoader imageLoader = getImageLoader();
                Bitmap m1 = imageLoader.loadImageSync(Uri.fromFile(f).toString(),targetSize);
                imageLoader.clearMemoryCache();

                document.newPage();
                String fileName =  file.getParent() + File.separator +"add_" +(UUID.randomUUID())+".png";
                saveToFile(m1, fileName);
            }
        }catch (DocumentException e){
            CustomLog.e(e.getMessage());
            e.printStackTrace();
        }catch (FileNotFoundException e){
            CustomLog.e(e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            CustomLog.e(e.getMessage());
            e.printStackTrace();
        }finally{
            try{if(document!=null)document.close();}catch(Exception ex){}
			/*try{if(writer!=null)writer.close();}catch(Exception ex){}
			try{if(reader!=null)reader.close();}catch(Exception ex){}*/
        }
    }

    public void absText(PdfWriter writer, String text, int x, int y) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont("assets/NanumSquareB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, y);
            cb.setFontAndSize(bf, 10);
            cb.showText(text);
            cb.endText();
            cb.restoreState();

            CustomLog.d("x="+x+", y="+y+", text="+text);
        } catch (NullPointerException e) {
        	CustomLog.e(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
        	CustomLog.e(e.getMessage());
        } catch (ClassCastException e) {
        	CustomLog.e(e.getMessage());
        } catch (Exception e) {
        	CustomLog.e(e.getMessage());
        }
    }
    public void absText(PdfWriter writer, String text, int x, int y,int size) {
        try {

            if (text == null)
                return;
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont("assets/NanumSquareB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, y);
            cb.setFontAndSize(bf, size);
            cb.showText(text);
            cb.endText();
            cb.restoreState();

            CustomLog.d("x="+x+", y="+y+", text="+text);
        }  catch (NullPointerException e) {
        	CustomLog.e(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
        	CustomLog.e(e.getMessage());
        } catch (ClassCastException e) {
        	CustomLog.e(e.getMessage());
        } catch (Exception e) {
        	CustomLog.e(e.getMessage());
        }
    }

    public void absText2(PdfWriter writer, String text, int x, int y) {
        try {

            if (text == null)
                return;
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont("assets/NanumSquareB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, y);
            cb.setFontAndSize(bf, 12);
            cb.setRGBColorFill(100,0,0);
            cb.showText(text);
            cb.endText();
            cb.restoreState();

            CustomLog.d("x="+x+", y="+y+", text="+text);
        } catch (NullPointerException e) {
        	CustomLog.e(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
        	CustomLog.e(e.getMessage());
        } catch (ClassCastException e) {
        	CustomLog.e(e.getMessage());
        } catch (Exception e) {
        	CustomLog.e(e.getMessage());
        }
    }

    public void absText3(PdfWriter writer, String text, int x, int y) {
        try {

            if (text == null)
                return;
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont("assets/NanumSquareB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, y);
            cb.setFontAndSize(bf, 8);
            cb.setRGBColorFill(100,0,0);
            cb.showText(text);
            cb.endText();
            cb.restoreState();

            CustomLog.d("x="+x+", y="+y+", text="+text);
        } catch (NullPointerException e) {
        	CustomLog.e(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
        	CustomLog.e(e.getMessage());
        } catch (ClassCastException e) {
        	CustomLog.e(e.getMessage());
        } catch (Exception e) {
        	CustomLog.e(e.getMessage());
        }
    }
    public void absText4(PdfWriter writer, String text, int x, int y) {
    	try {

            if (text == null)
                return;
    		PdfContentByte cb = writer.getDirectContent();
    		BaseFont bf = BaseFont.createFont("assets/NanumSquareB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		cb.saveState();
    		cb.beginText();
    		cb.moveText(x, y);
    		cb.setFontAndSize(bf, 14);
    		cb.setRGBColorFill(0,0,255);//파란색
    		cb.showText(text);
    		cb.endText();
    		cb.restoreState();

            CustomLog.d("x="+x+", y="+y+", text="+text);
    	} catch (NullPointerException e) {
    		CustomLog.e(e.getMessage());
    	} catch (IndexOutOfBoundsException e) {
    		CustomLog.e(e.getMessage());
    	} catch (ClassCastException e) {
    		CustomLog.e(e.getMessage());
    	} catch (Exception e) {
    		CustomLog.e(e.getMessage());
    	}
    }

    /**
     * 테스트 할 수 있도록 좌표를 찍어준다.
     * @param writer
     */
    public void Test(PdfWriter writer){
        for(int i =0; i < 1000; i=i+40){
            for(int j =0; j < 1000; j=j+10){
            	absText3(writer, ""+i+","+j,i, j);
            }
        }
       for(int i =0; i < 1000; i=i+10){
	       	for(int j =0; j < 1000; j=j+10){
	       		absText4(writer, ".",i, j);
	       	}
       }
    }

    public void saveToFile(Bitmap bmp, String filename) {
    	FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(CompressFormat.PNG, 100, out);

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

    public byte[] bitmapToByteArray( Bitmap $bitmap ) {
    	byte[] byteArray = null;
    	ByteArrayOutputStream stream = null;
    	try {
    	        stream = new ByteArrayOutputStream() ;
    	        $bitmap.compress( CompressFormat.PNG, 100, stream) ;
    	        byteArray = stream.toByteArray() ;
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
        		stream.close();
			} catch (IOException e) {
				CustomLog.e(e.getMessage());
			}
        }
    	        return byteArray ;
   }
}
