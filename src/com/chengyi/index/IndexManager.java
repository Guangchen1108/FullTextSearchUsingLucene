package com.chengyi.index;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hssf.usermodel.examples.AddDimensionedImage;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import com.chengyi.frame.LoginFrame;

/**
 * ��������
 * 
 * @author cxm
 *
 */
public class IndexManager {
	
	public static IndexManager indexManager;
	
	public static StringBuffer content = null;
	
	private static String INDEX_DIR = "D:\\luceneIndex";
	
	private static String DATA_DIR = "D:\\luceneData";
	
	private static Analyzer analyzer = null;
	
	private static Directory directory = null;
	
	private static IndexWriter indexWriter;
	
	// Singleton
	private IndexManager getManager() {
		if(indexManager == null) {
			this.indexManager = new IndexManager();
		}
		return indexManager;
	}
	
	/**
	 * ����Ŀ¼������
	 * 
	 * @param root
	 * @return
	 */
	public static boolean createIndexInDirectory(String root) {
		
		File file = new File(root);
		createIndexInAFile(root);
		if (file.exists()) {
			File[] files = file.listFiles();
			if(files.length == 0) {
				return Boolean.FALSE;
			} else {
				for (File file2 :files) {
					if (file2.isDirectory()) {
						createIndexInDirectory(file2.getAbsolutePath());
					} else {
						createIndexInAFile(file2.getAbsolutePath());
					}
				}
			}
		}
		
		return Boolean.TRUE;
	}
	
	/**
	 * ����������
	 * 
	 * @param path
	 * @return
	 */
	public static boolean createIndexInAFile(String path) {
		
		Date date = new Date();
		File dir = new File(path);
		if (!dir.isDirectory()) {
			return Boolean.FALSE;
		}
		List<File> fileList = getFileList(path);
		System.out.println("��ʼ�����ļ�����,Ŀ¼��" + path);
		for(File file : fileList) {
			
			content = new StringBuffer();
			
			String type = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			
			if ("txt".equalsIgnoreCase(type) || "html".equalsIgnoreCase(type)) {
				content.append(getTxtString(file));
			} else if ("doc".equalsIgnoreCase(type)) {
				content.append(getWordString(file));
			} else if ("xls".equalsIgnoreCase(type)) {
				content.append(getExcelString(file));
			} else if ("docx".equalsIgnoreCase(type)) {
				content.append(getDocxString(file));
			} else if ("xlsx".equalsIgnoreCase(type)) {
				content.append(getXlxsString(file));
			}
			
			//������ʼ
			Date startDate = new Date();
			try {
				analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
				directory = FSDirectory.open(new File(INDEX_DIR));
				File indexFile = new File(INDEX_DIR);
				if (!indexFile.exists()) {
					indexFile.mkdirs();
				}
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_CURRENT,analyzer);
				indexWriter = new IndexWriter(directory,config);
				System.out.print("���������ļ���" + file.getName());
				Document document = new Document();
				document.add(new TextField("filename",file.getName(),Store.YES));
				document.add(new TextField("content",content.toString(),Store.YES));
				document.add(new TextField("path",file.getPath(),Store.YES));
				indexWriter.addDocument(document);
				indexWriter.commit();
				indexWriter.close();
				System.out.println("------>������ʱ��" + ((new Date()).getTime() - startDate.getTime()) + "ms");
			} catch (Exception e) {
				e.printStackTrace();
			}
			content = null;
		}
		Date enDate = new Date();
		System.out.print("�ı�����" + fileList.size() + "��  ");
		System.out.println("������ʱ��" + (enDate.getTime() - date.getTime()) + "ms\n");
		return Boolean.TRUE;
	}

	/**
	 * ������ѯ
	 * 
	 * @param text
	 */
	public static List<String> searchIndex(String text) {
		
		List<String> list = new ArrayList<String>();
		Date startDate = new Date();
		System.out.println("�����ļ����ؼ���--->" + text);
		try {
			directory = FSDirectory.open(new File(INDEX_DIR));
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			DirectoryReader iReader = DirectoryReader.open(directory);
			IndexSearcher iSearcher = new IndexSearcher(iReader);
			QueryParser parser = new QueryParser(Version.LUCENE_40,"content",analyzer);
			Query query = parser.parse(text);
			ScoreDoc[] hits = iSearcher.search(query, null,1000).scoreDocs;
			System.out.print("���������" + hits.length + "��\n");
			for (int i=0;i<hits.length;i++) {
				Document hitDoc = iSearcher.doc(hits[i].doc);
				System.out.print("�ļ�����" + hitDoc.get("filename"));
				System.out.println("  |  �ļ�·����" + hitDoc.get("path"));
				list.add("[" + hitDoc.get("filename") + ","+ hitDoc.get("path") + "]");
				if (hitDoc.get("content").length() > 400) {
					list.add(hitDoc.get("content").substring(0,400) + "...");
				} else {
					list.add(hitDoc.get("content"));
				}
				
			}
			iReader.close();
			directory.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Date endDate = new Date();
		System.out.println("\n������ʱ:" + (endDate.getTime() - startDate.getTime()) + "ms\n");
		return list;

	}

	/**
	 * �����ı��ļ�
	 * 
	 * @param dir
	 * @return
	 */
	public static List<File> getFileList(String dir) {
		
		File[] files = new File(dir).listFiles();
		
		List<File> fileList = new ArrayList<File>();
		
		for(File file: files) {
			if (isTextFile(file.getName())) {
				fileList.add(file);
			}
		}
		return fileList;
		
	}
	/**
	 * �ж��Ƿ�Ϊ�ı��ļ�
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isTextFile(String fileName) {
		if (fileName.lastIndexOf(".txt") > 0) {
			return Boolean.TRUE;
		} else if (fileName.lastIndexOf(".xls") > 0) {
			return Boolean.TRUE;
		} else if (fileName.lastIndexOf("doc") > 0) {
			return Boolean.TRUE;
		} else if (fileName.lastIndexOf(".html") > 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	/**
	 * ��ȡtxt����
	 * 
	 * @param txtFile
	 * @return
	 */
	public static String getTxtString(File txtFile) {
		String result = "";
		try{
			BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile.getPath()),"UTF-8"));
			String s = null;
			while((s = bReader.readLine()) != null) {
				result = result + "\n" + s;
			}
			bReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ��ȡword�ļ����� using poi.hwpfDocument 93-07
	 * @param file
	 * @return
	 */
	public static String getWordString(File file) {
		String result = "";
		try {
			FileInputStream fis = new FileInputStream(file);
			HWPFDocument doc = new HWPFDocument(fis);
			Range range = doc.getRange();
			result += range.text();
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ��ȡword�ļ����� 07+
	 * 
	 * @param file
	 * @return
	 */
	public static String getDocxString(File file) {
		String result = "";
		try {
			OPCPackage opcPackage = POIXMLDocument.openPackage(file.getPath());
			POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
			result += extractor.getText().trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
		
	}
	
	
	
	/**
	 * ��ȡExcel���� using jxl.workbook 93-07
	 * 
	 * @param file
	 * @return
	 */
	public static String getExcelString(File file) {
		String result = "";
		try {
			FileInputStream fis = new FileInputStream(file);
			StringBuilder sb = new StringBuilder();
			jxl.Workbook rwb = Workbook.getWorkbook(fis);
			Sheet[] sheets = rwb.getSheets();
			for (int i = 0; i< sheets.length;i++) {
				Sheet sheet = sheets[i];
				for (int j = 0; j<sheet.getRows(); j++) {
					Cell[] cells = sheet.getRow(j);
					for (int k = 0; k<cells.length;k++) {
						sb.append(cells[k].getContents());
					}
				}
			}
			fis.close();
			result += sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * ��ȡExcel������ 2007+
	 * 
	 * @param file
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getXlxsString(File file) {
		String result ="";
		try {
			FileInputStream fis = new FileInputStream(file);
			
			org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(fis);
			for (int i = 0; i< wb.getNumberOfSheets(); i++) {
				org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(i);
				for (int j = 0;j<sheet.getLastRowNum();j++) {
					Row row = sheet.getRow(j);
					if (row == null) {
						continue;
					}
					for (int k = 0;k<=row.getLastCellNum();k++) {
						org.apache.poi.ss.usermodel.Cell cell = row.getCell(k);
						if(cell!=null){  
			                  cell.setCellType(1); 
			                  String contents = cell.getStringCellValue();
							result += contents;
			             } 
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ���Ŀ¼
	 * 
	 * @param file
	 * @return
	 */
	public static boolean deleteDir(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i=0;i<files.length;i++) {
				deleteDir(files[i]);
			}
		}
		file.delete();
		return true;
	}
	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// ��������ļ�
		File fileIndex = new File(INDEX_DIR);
		if(deleteDir(fileIndex)) {
			fileIndex.mkdir();
		} else {
			fileIndex.mkdir();
		}
		// ��������
		createIndexInDirectory(DATA_DIR);
		// ȫ�ļ���
		List<String> result = searchIndex("��");
		
		LoginFrame win = new LoginFrame("ȫ�ļ���");
		
		java.awt.List list = new java.awt.List(20000,false);
		
		list.add("�������һ��");
		
		for (String o : result) {
			list.add(o);
			System.out.println(o);
		}
		list.setBackground(Color.lightGray);
		list.setMultipleMode(true);
		win.add(list);
	}
}
